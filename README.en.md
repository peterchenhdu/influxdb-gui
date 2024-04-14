<p align="center">
	<img alt="logo" src="https://gitee.com/dbquery/influxdb-gui/raw/master/influxdb/src/main/resources/images/logo.png" width="150" height="150">
</p>

<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">InfluxDB-GUI v1.1.0</h1>
<p align="center">A free and open-source graphical user interface tool for the InfluxDB temporal database, providing users with an intuitive and easy-to-use database management and operation experience!</p>

----
English | [简体中文](README.md)

#### Description
InfluxDB GUI is a comprehensive and easy-to-use graphical interface tool for InfluxDB temporal databases. Currently, V1.0.0 supports the following versions of InfluxDB:
1. Influxdb 1.8.10 version

Main functions:
1. Data connection maintenance (adding, modifying, querying, deleting)
2. Database maintenance (adding, querying, deleting)
3. Measurement maintenance (querying, deleting)
4. Measurement data query (supports querying by time and automatic pagination display)
5. Data operation (copying field values)
6. Custom SQL execution (can execute updated SQL or query SQL, display query results, execution time, error information, etc.)
7. User View
8. Query monitoring (monitoring executing query SQL)

#### Software Architecture

Using JavaFX to Implement UI Functions.


#### Installation

1.  Download the latest version from the distribution, unzip it, and double-click to execute the Influxdb gui. exe file（ https://gitee.com/dbquery/influxdb-gui/releases ）

#### Instructions

1. Initial interface
![输入图片说明](influxdb/src/main/resources/images/readme/init-ui.png)
2. Data connection maintenance (adding, modifying, querying, deleting)
![输入图片说明](influxdb/src/main/resources/images/readme/connection-ui.png)
3. Database maintenance (adding, querying, deleting)
![输入图片说明](influxdb/src/main/resources/images/readme/database-ui.png)
4. MEASUREMENT maintenance (querying, deleting)
![输入图片说明](influxdb/src/main/resources/images/readme/measument-ui.png)
5. MEASUREMEN TData query (supports querying by time and automatic pagination display)
![输入图片说明](influxdb/src/main/resources/images/readme/measurement-query.png)
6. Data operation (copying field values)
![输入图片说明](influxdb/src/main/resources/images/readme/data-copy.png)
7. Custom SQL execution (capable of updating or querying SQL, displaying query results, execution time, error information, etc.)
![输入图片说明](influxdb/src/main/resources/images/readme/custom-query.png)
8. User View
![输入图片说明](influxdb/src/main/resources/images/readme/user-query.png)
9. Query monitoring (monitoring executing query SQL)
![输入图片说明](influxdb/src/main/resources/images/readme/query-monitor.png)
####Contribution

Fork the repository
Create Feat_xxx branch
Commit your code
Create Pull Request
