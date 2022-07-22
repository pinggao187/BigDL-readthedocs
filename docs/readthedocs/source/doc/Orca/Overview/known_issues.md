# Orca Known Issues

## **Estimator Issues**

### **OSError: Unable to load libhdfs: ./libhdfs.so: cannot open shared object file: No such file or directory**

This error occurs while running Orca with `yarn-client` mode on Cloudera, where PyArrow failed to locate `libhdfs.so` in default path of `$HADOOP_HOME/lib/native`. To solve this, we need to set the path of `libhdfs.so` in Cloudera to the environment variable of `ARROW_LIBHDFS_DIR` on spark executors. 

You could follow below steps:

1. use `locate libhdfs.so` to find `libhdfs.so`
2. `export ARROW_LIBHDFS_DIR=/opt/cloudera/parcels/CDH-5.15.2-1.cdh5.15.2.p0.3/lib64` (replace with the result of locate libhdfs.so)
3. If you are using `init_orca_context(cluster_mode="yarn-client")`: 
   ```
   conf = {"spark.executorEnv.ARROW_LIBHDFS_DIR": "/opt/cloudera/parcels/CDH-5.15.2-1.cdh5.15.2.p0.3/lib64"}
   init_orca_context(cluster_mode="yarn", conf=conf)
   ```
   If you are using `init_orca_context(cluster_mode="spark-submit")`:
   ```
   spark-submit --conf "spark.executorEnv.ARROW_LIBHDFS_DIR=/opt/cloudera/parcels/CDH-5.15.2-1.cdh5.15.2.p0.3/lib64"
   ```

## **Orca Context Issues**

### **Exception: Failed to read dashbord log: [Errno 2] No such file or directory: '/tmp/ray/.../dashboard.log'**

This error occurs when initialize an orca context with `init_ray_on_spark=True`. We have not locate the root cause of this problem, but it might be caused by an atypical python environment.

You could follow below steps to workaround:

1. If you only need to use functions in ray (e.g. `bigdl.orca.learn` with `backend="ray"`, `bigdl.orca.automl` for pytorch/tensorflow model, `bigdl.chronos.autots` for time series model's auto-tunning), we may use ray as the first-class.

   1. Start a ray cluster by `ray start --head`. if you already have a ray cluster started, please direcetly jump to step 2.
   2. Initialize an orca context with `runtime="ray"` and `init_ray_on_spark=False`, please refer to detailed information [here](./orca-context.html).
   3. If you are using `bigdl.orca.automl` or `bigdl.chronos.autots` on a single node, please set:
      ```python
      ray_ctx = OrcaContext.get_ray_context()
      ray_ctx.is_local=True
      ```

2. If you really need to use ray on spark, please install bigdl-orca under a conda environment. Detailed information please refer to [here](./orca.html).