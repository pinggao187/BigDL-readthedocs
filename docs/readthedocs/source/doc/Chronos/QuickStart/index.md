# Chronos Tutorial

- [**Predict Number of Taxi Passengers with Chronos Forecaster**](./chronos-tsdataset-forecaster-quickstart.html)

    > ![](../../../../image/colab_logo_32px.png)[Run in Google Colab][chronos_nyc_taxi_tsdataset_forecaster_colab] &nbsp;![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][chronos_nyc_taxi_tsdataset_forecaster]

    In this guide we will demonstrate how to use _Chronos TSDataset_ and _Chronos Forecaster_ for time series processing and predict number of taxi passengers.

---------------------------

- [**Tune a Forecasting Task Automatically**](./chronos-autotsest-quickstart.html)

    > ![](../../../../image/colab_logo_32px.png)[Run in Google Colab][chronos_autots_nyc_taxi_colab] &nbsp;![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][chronos_autots_nyc_taxi]

    In this guide we will demonstrate how to use _Chronos AutoTSEstimator_ and _Chronos TSPipeline_ to auto tune a time seires forecasting task and handle the whole model development process easily.

---------------------------

- [**Detect Anomaly Point in Real Time Traffic Data**](./chronos-anomaly-detector.html)

    > ![](../../../../image/colab_logo_32px.png)[Run in Google Colab][chronos_minn_traffic_anomaly_detector_colab] &nbsp;![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][chronos_minn_traffic_anomaly_detector]

    In this guide we will demonstrate how to use _Chronos Anomaly Detector_ for real time traffic data from the Twin Cities Metro area in Minnesota anomaly detection.

---------------------------

- [**Tune a Customized Time Series Forecasting Model with AutoTSEstimator.**][network_traffic_autots_customized_model]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][network_traffic_autots_customized_model]

    In this notebook, we demonstrate a reference use case where we use the network traffic KPI(s) in the past to predict traffic KPI(s) in the future. We demonstrate how to use _AutoTSEstimator_ to adjust the parameters of a customized model.

---------------------------

- [**Auto Tune the Prediction of Network Traffic at the Transit Link of WIDE**][network_traffic_autots_forecasting]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][network_traffic_autots_forecasting]

    In this notebook, we demostrate a reference use case where we use the network traffic KPI(s) in the past to predict traffic KPI(s) in the future. We demostrate how to use _AutoTS_ in project [Chronos][chronos] to do time series forecasting in an automated and distributed way.

---------------------------

- [**Multivariate Forecasting of Network Traffic at the Transit Link of WIDE**][network_traffic_model_forecasting]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][network_traffic_model_forecasting]

    In this notebook, we demonstrate a reference use case where we use the network traffic KPI(s) in the past to predict traffic KPI(s) in the future. We demostrate how to do univariate forecasting (predict only 1 series), and multivariate forecasting (predicts more than 1 series at the same time) using Project [Chronos][chronos].

---------------------------

- [**Multistep Forecasting of Network Traffic at the Transit Link of WIDE**][network_traffic_multivariate_multistep_tcnforecaster]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][network_traffic_multivariate_multistep_tcnforecaster]

    In this notebook, we demonstrate a reference use case where we use the network traffic KPI(s) in the past to predict traffic KPI(s) in the future. We demostrate how to do multivariate multistep forecasting using Project [Chronos][chronos].

---------------------------

- [**Stock Price Prediction with LSTMForecaster**][stock_prediction]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][stock_prediction]

    In this notebook, we demonstrate a reference use case where we use historical stock price data to predict the future price. The dataset we use is the daily stock price of S&P500 stocks during 2013-2018 (data source). We demostrate how to do univariate forecasting using the past 80% of the total days' MMM price to predict the future 20% days' daily price.

    Reference: *<https://github.com/jwkanggist/tf-keras-stock-pred>*

---------------------------

- [**Stock Price Prediction with ProphetForecaster and AutoProphet**][stock_prediction_prophet]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][stock_prediction_prophet]

    In this notebook, we demonstrate a reference use case where we use historical stock price data to predict the future price using the ProphetForecaster and AutoProphet. The dataset we use is the daily stock price of S&P500 stocks during 2013-2018 [data source](https://www.kaggle.com/camnugent/sandp500/).

    Reference: *<https://facebook.github.io/prophet>*, *<https://github.com/jwkanggist/tf-keras-stock-pred>*

---------------------------

- [**Unsupervised Anomaly Detection for CPU Usage**][AIOps_anomaly_detect_unsupervised]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][AIOps_anomaly_detect_unsupervised]

    We demonstrates how to perform anomaly detection based on Chronos's built-in [DBScanDetector][DBScan], [AEDetector][AE] and [ThresholdDetector][Threshold].

---------------------------

- [**Anomaly Detection for CPU Usage Based on Forecasters**][AIOps_anomaly_detect_unsupervised_forecast_based]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][AIOps_anomaly_detect_unsupervised_forecast_based]

    We demonstrates how to leverage Chronos's built-in models ie. MTNet, to do time series forecasting. Then perform anomaly detection on predicted value with [ThresholdDetector][Threshold].

---------------------------

- [**Help pytorch-forecasting improve the training speed of DeepAR model**][pytorch_forecasting_deepar]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][pytorch_forecasting_deepar]

    Chronos can help a 3rd party time series lib to improve the performance (both training and inferencing) and accuracy. This use-case shows Chronos can easily help pytorch-forecasting speed up the training of DeepAR model.

---------------------------

- [**Help pytorch-forecasting improve the training speed of TFT model**][pytorch_forecasting_tft]

    > ![](../../../../image/GitHub-Mark-32px.png)[View source on GitHub][pytorch_forecasting_tft]

    Chronos can help a 3rd party time series lib to improve the performance (both training and inferencing) and accuracy. This use-case shows Chronos can easily help pytorch-forecasting speed up the training of TFT model.


[DBScan]: <../../PythonAPI/Chronos/anomaly_detectors.html#dbscandetector>
[AE]: <../../PythonAPI/Chronos/anomaly_detectors.html#aedetector>
[Threshold]: <../../PythonAPI/Chronos/anomaly_detectors.html#thresholddetector>
[chronos]: <https://github.com/intel-analytics/bigdl/tree/main/python/chronos/src/bigdl/chronos>
[chronos_nyc_taxi_tsdataset_forecaster_colab]: <https://colab.research.google.com/github/intel-analytics/BigDL/blob/main/python/chronos/colab-notebook/chronos_nyc_taxi_tsdataset_forecaster.ipynb>
[chronos_nyc_taxi_tsdataset_forecaster]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/colab-notebook/chronos_nyc_taxi_tsdataset_forecaster.ipynb>
[chronos_autots_nyc_taxi_colab]: <https://colab.research.google.com/github/intel-analytics/BigDL/blob/main/python/chronos/colab-notebook/chronos_autots_nyc_taxi.ipynb>
[chronos_autots_nyc_taxi]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/colab-notebook/chronos_autots_nyc_taxi.ipynb>
[chronos_minn_traffic_anomaly_detector_colab]: <https://colab.research.google.com/github/intel-analytics/BigDL/blob/main/python/chronos/colab-notebook/chronos_minn_traffic_anomaly_detector.ipynb>
[chronos_minn_traffic_anomaly_detector]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/colab-notebook/chronos_minn_traffic_anomaly_detector.ipynb>
[network_traffic_autots_customized_model]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/network_traffic/network_traffic_autots_customized_model.ipynb>
[network_traffic_autots_forecasting]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/network_traffic/network_traffic_autots_forecasting.ipynb>
[network_traffic_model_forecasting]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/network_traffic/network_traffic_model_forecasting.ipynb>
[network_traffic_multivariate_multistep_tcnforecaster]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/network_traffic/network_traffic_multivariate_multistep_tcnforecaster.ipynb>
[stock_prediction]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/fsi/stock_prediction.ipynb>
[stock_prediction_prophet]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/fsi/stock_prediction_prophet.ipynb>
[AIOps_anomaly_detect_unsupervised]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/AIOps/AIOps_anomaly_detect_unsupervised.ipynb>
[AIOps_anomaly_detect_unsupervised_forecast_based]: <https://github.com/intel-analytics/BigDL/blob/main/python/chronos/use-case/AIOps/AIOps_anomaly_detect_unsupervised_forecast_based.ipynb>
[pytorch_forecasting_deepar]: <https://github.com/intel-analytics/BigDL/tree/main/python/chronos/use-case/pytorch-forecasting/DeepAR>
[pytorch_forecasting_tft]: <https://github.com/intel-analytics/BigDL/tree/main/python/chronos/use-case/pytorch-forecasting/TFT>
