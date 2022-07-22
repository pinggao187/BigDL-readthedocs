/*
 * Copyright 2021 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.bigdl.friesian.serving.feature;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.protobuf.Empty;
import com.intel.analytics.bigdl.friesian.serving.feature.utils.LettuceUtils;
import com.intel.analytics.bigdl.friesian.serving.grpc.generated.feature.FeatureGrpc;
import com.intel.analytics.bigdl.friesian.serving.grpc.generated.feature.FeatureProto.Features;
import com.intel.analytics.bigdl.friesian.serving.grpc.generated.feature.FeatureProto.IDs;
import com.intel.analytics.bigdl.friesian.serving.grpc.generated.feature.FeatureProto.ServerMessage;
import com.intel.analytics.bigdl.friesian.serving.utils.TimerMetrics;
import com.intel.analytics.bigdl.friesian.serving.utils.TimerMetrics$;
import com.intel.analytics.bigdl.friesian.serving.utils.Utils;
import com.intel.analytics.bigdl.friesian.serving.utils.feature.FeatureUtils;
import com.intel.analytics.bigdl.friesian.serving.utils.gRPCHelper;
import com.intel.analytics.bigdl.grpc.GrpcServerBase;
import com.intel.analytics.bigdl.grpc.JacksonJsonSerializer;
import com.intel.analytics.bigdl.orca.inference.InferenceModel;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import io.prometheus.client.exporter.HTTPServer;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringServerInterceptor;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

enum ServiceType {
    KV, INFERENCE
}

enum SearchType {
    ITEM, USER
}

public class FeatureServer extends GrpcServerBase {
    private static final Logger logger = LogManager.getLogger(FeatureServer.class.getName());

    /** Create a Feature server. */
    public FeatureServer(String[] args) {
        super(args);
        port = 8082;
        configPath = "config_feature.yaml";
        options.addOption(new Option("p", "port", true,
                "The port to create the server"));
        Configurator.setLevel("org", Level.ERROR);
    }

    @Override
    public void parseConfig() throws Exception {
        Utils.helper_$eq(getConfigFromYaml(gRPCHelper.class, configPath));
        Utils.helper().parseConfigStrings();
        if (Utils.helper() != null && Utils.helper().getServicePort() != -1) {
            port = Utils.helper().getServicePort();
        } else if (cmd.getOptionValue("port") != null) {
            port = Integer.parseInt(cmd.getOptionValue("port"));
        }

        if (Utils.runMonitor()) {
            logger.info("Starting monitoringInterceptor....");
            MonitoringServerInterceptor monitoringInterceptor =
                    MonitoringServerInterceptor.create(Configuration.allMetrics()
                            .withLatencyBuckets(Utils.getPromBuckets()));
            serverDefinitionServices.add(ServerInterceptors
                    .intercept(new FeatureService().bindService(), monitoringInterceptor));
        } else {
            serverServices.add(new FeatureService());
        }
        serverServices.add(new HealthStatusManager().getHealthService());
    }

    /**
     * Main method.  This comment makes the linter happy.
     */
    public static void main(String[] args) throws Exception {
        FeatureServer featureServer = new FeatureServer(args);
        featureServer.build();
        if (Utils.runMonitor()) {
            new HTTPServer.Builder()
                    .withPort(Utils.helper().monitorPort()).build();
        }
        featureServer.start();
        featureServer.blockUntilShutdown();
    }

    private static class FeatureService extends FeatureGrpc.FeatureImplBase {
        private InferenceModel userModel;
        private InferenceModel itemModel;
        private LettuceUtils redis;
        private Set<ServiceType> serviceType;
        private Map<String, String[]> colNamesMap;
        private MetricRegistry metrics = new MetricRegistry();
        Timer overallTimer = metrics.timer("feature.overall");
        Timer userPredictTimer = metrics.timer("feature.user.predict");
        Timer itemPredictTimer = metrics.timer("feature.item.predict");
        Timer userRedisTimer = metrics.timer("feature.user.redis");
        Timer itemRedisTimer = metrics.timer("feature.item.redis");

        FeatureService() throws Exception {
            serviceType = new HashSet<>();
            colNamesMap = new HashMap<>();
            parseServiceType();
            if (serviceType.contains(ServiceType.KV)) {
                redis = LettuceUtils.getInstance(Utils.helper().redisTypeEnum(), Utils.helper().redisHostPort(),
                        Utils.helper().getRedisKeyPrefix(), Utils.helper().redisSentinelMasterURL(),
                        Utils.helper().redisSentinelMasterName(), Utils.helper().itemSlotType());
            }

            if (serviceType.contains(ServiceType.INFERENCE)) {
                if (Utils.helper().getUserModelPath() != null) {
                    userModel = Utils.helper()
                            .loadInferenceModel(Utils.helper().getModelParallelism(),
                                    Utils.helper().getUserModelPath(), null);
                }
                if (Utils.helper().getItemModelPath() != null){
                    itemModel = Utils.helper()
                            .loadInferenceModel(Utils.helper().getModelParallelism(),
                                    Utils.helper().getItemModelPath(), null);
                }
                if (userModel == null && itemModel == null) {
                    throw new Exception("Either userModelPath or itemModelPath should be provided.");
                }
            }

            if (Utils.helper().logInterval() > 0) {
                MetricAttribute[] disAttr = {MetricAttribute.STDDEV, MetricAttribute.P75,
                        MetricAttribute.P999, MetricAttribute.P98, MetricAttribute.M5_RATE,
                        MetricAttribute.M15_RATE};
                ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .disabledMetricAttributes(new HashSet<>(Arrays.asList(disAttr)))
                        .build();
                reporter.start(Utils.helper().logInterval(), TimeUnit.MINUTES);
            }
        }

        void parseServiceType() {
            Map<String, ServiceType> typeMap = new HashMap<String, ServiceType>() {{
                put("kv", ServiceType.KV);
                put("inference", ServiceType.INFERENCE);
            }};
            String[] typeArray = Utils.helper().serviceType().split("\\s*,\\s*");
            for (String typeStr : typeArray) {
                serviceType.add(typeMap.get(typeStr));
            }
        }
        @Override
        public void getUserFeatures(IDs request,
                                    StreamObserver<Features> responseObserver) {
            Features result;
            try {
                result = getFeatures(request, SearchType.USER);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e.getMessage());
                responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage())
                        .asRuntimeException());
                return;
            }
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        }

        @Override
        public void getItemFeatures(IDs request,
                                    StreamObserver<Features> responseObserver) {
            Features result;
            try {
                result = getFeatures(request, SearchType.ITEM);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e.getMessage());
                responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage())
                        .asRuntimeException());
                return;
            }
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        }

        @Override
        public void getMetrics(Empty request,
                               StreamObserver<ServerMessage> responseObserver) {
            responseObserver.onNext(getMetrics());
            responseObserver.onCompleted();
        }

        private Features getFeatures(IDs msg, SearchType searchType) throws Exception {
            Timer.Context overallContext = overallTimer.time();
            Features result;
            if (serviceType.contains(ServiceType.KV) &&
                    serviceType.contains(ServiceType.INFERENCE)) {
                result = getFeaturesFromRedisAndInference(msg, searchType);
            }
            else if (serviceType.contains(ServiceType.KV)) {
                result = getFeaturesFromRedis(msg, searchType);
            } else if (serviceType.contains(ServiceType.INFERENCE)){
                result = getFeaturesFromInferenceModel(msg, searchType);
            } else {
                throw new Exception("ServiceType is not supported, only 'kv', 'inference' and " +
                        "'kv, inference' are supported");
            }

            overallContext.stop();

            return result;
        }

        private Features getFeaturesFromRedisAndInference(IDs msg, SearchType searchType)
                throws Exception {
            Features.Builder featureBuilder = Features.newBuilder();
            Timer.Context predictContext;
            String typeStr = "";
            InferenceModel model;
            String[] featureCols;
            Features features = getFeaturesFromRedis(msg, searchType);
            if (searchType == SearchType.USER) {
                predictContext = userPredictTimer.time();
                model = this.userModel;
                typeStr = "user";
                featureCols = Utils.helper().userFeatureColArr();
            } else {
                predictContext = itemPredictTimer.time();
                model = this.itemModel;
                typeStr = "item";
                featureCols = Utils.helper().itemFeatureColArr();
            }
            if (model == null) {
                throw new Exception(typeStr + "ModelPath should be provided in the config.yaml " +
                        "file");
            }
            List<String> result = FeatureUtils.predictFeatures(features, model, featureCols);
            for (String feature: result) {
                featureBuilder.addB64Feature(feature);
            }
            predictContext.close();
            return featureBuilder.build();
        }

        private Features getFeaturesFromRedis(IDs msg, SearchType searchType) {
            String keyPrefix = searchType == SearchType.USER ? "user": "item";
            List<Integer> ids = msg.getIDList();

            Features.Builder featureBuilder = Features.newBuilder();
            Timer.Context redisContext;
            if (searchType == SearchType.USER) {
                redisContext = userRedisTimer.time();
            } else {
                redisContext = itemRedisTimer.time();
            }

            List<String> values = redis.MGet(keyPrefix, ids);
            featureBuilder.addAllID(ids);
            featureBuilder.addAllB64Feature(values);
            redisContext.stop();
            if (!colNamesMap.containsKey(keyPrefix)) {
                String colNamesStr;
                colNamesStr = redis.getSchema(keyPrefix);
                colNamesMap.put(keyPrefix, colNamesStr.split(","));
            }
            featureBuilder.addAllColNames(Arrays.asList(colNamesMap.get(keyPrefix)));

            return featureBuilder.build();
        }

        private Features getFeaturesFromInferenceModel(IDs msg, SearchType searchType) throws Exception {
            Features.Builder featureBuilder = Features.newBuilder();
            Timer.Context predictContext;
            String typeStr = "";
            InferenceModel model;
            if (searchType == SearchType.USER) {
                predictContext = userPredictTimer.time();
                model = this.userModel;
                typeStr = "user";
            } else {
                predictContext = itemPredictTimer.time();
                model = this.itemModel;
                typeStr = "item";
            }
            if (model == null) {
                throw new Exception(typeStr + "ModelPath should be provided in the config.yaml " +
                        "file");
            }
            List<String> result = FeatureUtils.doPredict(msg, model);
            for (String feature: result) {
                featureBuilder.addB64Feature(feature);
            }
            predictContext.close();
            return featureBuilder.build();
        }

        private ServerMessage getMetrics() {
            JacksonJsonSerializer jacksonJsonSerializer = new JacksonJsonSerializer();
            Set<String> keys = metrics.getTimers().keySet();
            List<TimerMetrics> timerMetrics = keys.stream()
                    .map(key ->
                            TimerMetrics$.MODULE$.apply(key, metrics.getTimers().get(key)))
                    .collect(Collectors.toList());
            String jsonStr = jacksonJsonSerializer.serialize(timerMetrics);
            return ServerMessage.newBuilder().setStr(jsonStr).build();
        }

        @Override
        public void resetMetrics(Empty request, StreamObserver<Empty> responseObserver) {
            metrics = new MetricRegistry();
            overallTimer = metrics.timer("feature.overall");
            userPredictTimer = metrics.timer("feature.user.predict");
            itemPredictTimer = metrics.timer("feature.item.predict");
            userRedisTimer = metrics.timer("feature.user.redis");
            itemRedisTimer = metrics.timer("feature.item.redis");
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }
    }
}
