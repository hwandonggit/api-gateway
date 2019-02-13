# bio-gateway 0.9

This is a scala inplementation of bioinfx gateway

## Getting Started

### 1. Install Redis Server

The service uses [Redis](https://redis.io/) to implement message queue:
[![Get Redis](https://en.wikipedia.org/wiki/File:Redis_Logo.svg)](https://redis.io/download)

After installation, open `conf/application.conf` and modify the following lines:

```script
# enable redis cache module
redis {
  host="localhost"
  port=6379
  timeout=5000
  task_queue_key="tasks_queue"
  done_task_queue_key="done_task_queue"
  failed_task_queue_key="failed_task_queue"
  auto_incr_key="unique_id"
  task_key_prefix="TXS"
  sub_task_queue_prefix="BATCH@"
  sub_task_queue_paths_prefix="PATH@"
  token_counter="TC@"
  key_timeout=86400
}


```

## Running the Project

Run this using [sbt](http://www.scala-sbt.org/). 

```
sbt run
```

And then go to http://localhost:9000 to see the running web application.

## Delpoy in Production
 [Play2.6 Deploy](https://www.playframework.com/documentation/2.6.x/Deploying)

To archive the production package, run following cmd:

```
sbt dist
```
This produces a ZIP file containing all JAR files needed to run your application in the target/universal folder of your project.
Copy the zip file to the remote server you want to make production deployed and then unzip it using cmd:

```
unzip ???-0-1.0-SNAPSHOT.zip
```

Finaly, change the path to the `bin` folder and launch the production by using:

```
./es2-0 -Dplay.http.secret.key=xxxxxxxxxx -Dhttp.port=9001 -Dconfig.file=/mnt/fvg01vol2/ES_SCALA/es2-0-1.0-SNAPSHOT/conf/production.conf -Dlogger.file=./conf/production_logback.xml
```
`xxxxxxxxx` is basically a customized application secret .

And then go to http://host:9000 to see the running web application.

## API Usage

The APIs are configured in `conf/routes`

### A transaction queue monitor
| HTTP METHOD | URI             | USAGE     											|
| ----------- | --------------- | --------------- 										|
| GET     	  | /          		| Rendering the website homepage	        			| 


| HTTP METHOD | URI             | USAGE     											|
| ----------- | --------------- | --------------- 										|
| GET         | /:id            | Rendering the detail information popout for an audit 	|

                             
### Website resources
| HTTP METHOD | URI             | USAGE     														 	|
| ----------- | --------------- | --------------- 														|
| GET         | /assets/*file   | Map static resources from the /public folder to the /assets URL path 	|
       
### Task queue controller
| HTTP METHOD | URI             | USAGE     														 	|
| ----------- | --------------- | --------------- 														|
| GET         | /queue/list     | List all transaction inside redis 	                                |
   
- Example
```javascript
GET 10.180.2.19:9001/queue/list

```
- Response
```javascript
{"result":"SUCCESS","transactions":[]}

```
 
| HTTP METHOD | URI             | USAGE     														 	|
| ----------- | --------------- | --------------- 														|
| GET         | /queue/recover  | Recover an aborted transaction after ES restarting 	                |

- Example
```javascript
GET 10.180.2.19:9001/queue/recover?id=ES1000835

```
 
- Response
```javascript
{"result":"SUCCESS", "msg":"Recovering transaction: ES1000835", "source":"ESQueueController"}

```        
 
### Accession controller
| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /jsonEnrollAccession    | Accession level data enrollment 	|

- Params
```javascript
{
	"libraryid":"NS490",
	"accessionid":"1612752",
	"runfolder":"NS490_160416_K00177_0070_BH7NCMBBXX",
	"captureset":"TS1Ev1",
	"bamsubfolder": "/path/to/bam/subfolder",
	"datatype":"BAM",
	"tooltype":"drangen",
	"datapath":"/path/of/data",
	"version":"1.0",			
	"ext_id":"TXS000835"
}

```

- Response
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
  		{
			"id": "TXS000066",
			"recordid": "CVT-ES44070_1612752",
			"accession_datapath": "/path/of/perm/data"
		}
	],
	"message": "begin enrolling"
}

```


| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /jsonDeleteAccession    | Accession level data clean up  	|

- Params
```javascript
{
  "accessionid": "1612752",
  "libraryid": "CX030",
  "runfolder": "160601_K00177_0084_AHCNLNBBXX",
  "captureset": "TS1Ev1",
  "datatype": "ACC_AUDIT",
  "tooltype":"drangen",
  "datapath": "/path/of/perm/data",
  "version": "1.0",
  "user": "PLM",
  "ext_id":"TXS000835"
}

```

- Response      
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
  		{
			"id": "TXS000067",
			"recordid": "CVT-ES31785_TXS000067",
			"delete_datapath": "/path/of/perm/data/ES31785_TXS000067_acc_audit.zip"
		},
 		{
			"id": "TXS000078",
			"recordid": "CVT-ES31815_TXS000078",
			"delete_datapath": "/path/of/perm/data/ES31785_TXS000078_acc_audit.zip"
		}
	],
	"message": "begin deleting"
}     

```    

| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /jsonEnrollSomaticAccessionDataset    | Enroll Somatic Accession Level Dataset  	|

- Params
```javascript
{
  "accessionid": "1612752",
  "libraryid": "CX030",
  "runfolder": "160601_K00177_0084_AHCNLNBBXX",
  "captureset": "TS1Ev1",
  "datatype": "ACC_AUDIT",
  "tooltype":"drangen",
  "datapath": "/path/of/perm/data",
  "eventId": "testevent",
  "state": "wait_prepData",
  "version": "1.0",
  "ext_id":"TXS000835"
}

```

- Response      
```javascript
{
	
}     

```  

| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /jsonRetrieveSomaticAccessionDataset    | Retrieve Somatic Accession Level Dataset  	|

- Params
```javascript
{
 
}

```

- Response      
```javascript
{
	
}     

```  
 
### Library controller
| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /jsonEnrollLibrary    	| Library level data enrollment  	|                   

- Params
```javascript
{
	"libraryid":"NS490",
	"runfolder":"NS490_160416_K00177_0070_BH7NCMBBXX",
	"captureset":"TS1Ev1",
	"datatype":"ACC_LIB",
	"tooltype":"",
	"datapath":"/path/of/data",
	"version":"1.0",
	"ext_id":"TXS000835" 										
}

- Response
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
  		{
			"id": "TXS000066",
			"recordid": "CVT-ES44070_1612752",
			"accession_datapath": "/path/of/perm/data"
		}
	],
	"message": "begin enrolling"
}

```


| HTTP METHOD | URI             	 | USAGE     					  	|
| ----------- | -------------------- | --------------- 					|
| POST        | /jsonDeleteLibrary	 | Library level data clean up  	|  

- Params
```javascript
{
  "libraryid": "CX030",
  "runfolder": "160601_K00177_0084_AHCNLNBBXX",
  "captureset": "TS1Ev1",
  "datatype": "ACC_LIB",
  "tooltype": "NOVA",
  "datapath": "/path/of/perm/data",
  "version": "1.0",
  "user": "PLM",
  "ext_id":"TXS000835"
}

```

- Response      
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
  		{
			"id": "TXS000067",
			"recordid": "CVT-ES31785_TXS000067",
			"delete_datapath": "/path/of/perm/data/ES31785_TXS000067_acc_audit.zip"
		},
 		{
			"id": "TXS000078",
			"recordid": "CVT-ES31815_TXS000078",
			"delete_datapath": "/path/of/perm/data/ES31785_TXS000078_acc_audit.zip"
		}
	],
	"message": "begin deleting"
}     

```              

### Panel controller
| HTTP METHOD | URI             	 | USAGE     				   |
| ----------- | -------------------- | --------------------------- |
| POST        | /jsonEnrollPaneltest | Panel level data enrollment |  

- Params
```javascript
{
  "accessionid": "AC45678",
  "libraryid": "CX030",
  "runfolder": "160601_K00177_0084_AHCNLNBBXX",
  "captureset": "TS1Ev1",
  "datatype": "FV",
  "tooltype": "NOVA",
  "panelname": "PANELNAME",
  "testid": "TESTID",
  "datapath": "path/of/data",
  "version": "1.0",
  "ext_id":"TXS000835"
}

```

- Response      
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
	],
	"message": "begin enrolling"
}  
 
```  


| HTTP METHOD | URI             	 | USAGE     				 |
| ----------- | -------------------- | ------------------------- |
| POST        | /jsonDeletePaneltest | Panel level data clean up |  
            
- Params
```javascript
{
  "accessionid": "TX45678",
  "libraryid": "CX030",
  "runfolder": "160601_K00177_0084_AHCNLNBBXX",
  "captureset": "TS1Ev1",
  "datatype": "PANEL_AUDIT",
  "tooltype": "NOVA",
  "datapath": "/path/of/perm/data",
  "version": "1.0",
  "user": "PLM",
  "ext_id":"TXS000835"
}

```

- Response      
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
	],
	"message": "begin deleting"
}    
 
```                

### Run controller
| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /jsonArchive    		| Archive the running folder  		|  
                     
- Params
```javascript
{ 
	"runfolder": "160601_K00177_0084_AHCNLNBBXX", 
	"user": "plm",
	"parentPath": "/path/of/parentpath", 
	"version": "1.0",
	"ext_id":"TXS000835"
}

```

- Response      
```javascript
{
	"result": "SUCCESS",
	"message": "begin archiving"
}  
 
```

| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /jsonCheckFolderSize    | Check a folder size in fs   		|  

- Params
```javascript
{ 
	"datapath": "/mnt/fvg01vol5/PRD_171024_A00294_0005_AH3VWLDMXX", 
	"resolveSymLink": true, // if true, will dereference symbolic link and counting on the target file size.
	"version": "1.0"
}

```

- Response      
```javascript
{
    "result": "SUCCESS",
    "info": {
        "Size (KB)": 723500695,
        "Original Path": "/mnt/fvg01vol5/PRD_171024_A00294_0005_AH3VWLDMXX",
        "File Name": "PRD_171024_A00294_0005_AH3VWLDMXX"
    }
}
```

  
                     
### Audit controller
| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| GET         | /auditFetch    			| Fetch audits 						| 

- Params
```javascript
{

}

```              
- Response      
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
  		{
			"id": "TXS000077",
			"recordid": "CVT-ES31814_ttttt",
			"delete_datapath": "/path/of/perm/data/CVT-ES31814_TXS000077_acc_audit.zip"
		},
  		{
			"id": "TXS000078",
			"recordid": "CVT-ES31815_ttttt",
			"delete_datapath": "/path/of/perm/data/CVT-ES31815_TXS000078_acc_audit.zip"
		}
	],
	"message": "begin delete"
}   
 
```

| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /auditFilter    		| fetch audits by using filter  	| 
                          
- Params
```javascript
{
  
}

```

- Response      
```javascript
{
	
}  
   
```                  

### HDFS controller
| HTTP METHOD | URI             		| USAGE     					  	|
| ----------- | --------------- 		| --------------- 					|
| POST        | /hdfsArchiveFile    	| Archive a single file into hdfs  	| 

- Params
```javascript
{
  "parent": "/path/of/perm/data,
  "file": "filename",
  "version": "1.0",
  "user": "PLM"
}

```

- Response      
```javascript
{
	"result": "SUCCESS",
	"keyValueTable": [
  		{
			"id": "TXS000067",
			"recordid": "CVT-ES31785_TXS000067",
			"delete_datapath": "/path/of/perm/data/ES31785_TXS000067_acc_audit.zip"
		},
 		{
			"id": "TXS000078",
			"recordid": "CVT-ES31815_TXS000078",
			"delete_datapath": "/path/of/perm/data/ES31785_TXS000078_acc_audit.zip"
		}
	],
	"message": "begin deleting"
}    

``` 
                        
| HTTP METHOD | URI             		| USAGE     					  		|
| ----------- | --------------- 		| --------------- 						|
| POST        | /hdfsArchiveFolder    	| Archive a running folder into hdfs  	| 

- Params
```javascript
{
  "runfolder": "160601_K00177_0084_AHCNLNBBXX",
  "datapath": "/path/of/hdfs/system",
  "version": "1.0",
  "user": "PLM"
}

```

- Response      
```javascript
{
	"result": "SUCCESS",
	"message": "begin retrieving"
}

```     

### FLIM APIs
| HTTP METHOD | URI             		| USAGE     					  		            |
| ----------- | --------------- 		| --------------- 						            |
| GET         | /flim/checkBam      	| Check if the BAM files has been enrolled or not  	| 

- Arguments
```
accessionid=xxxxxxxx&captureset=xxxxxxxx
```

- Response      
```javascript
code: 200
{"result":"SUCCESS"}

```   
```javascript
code: 400
{"result":"ERROR","keyValueTable":[],"message":"Can not find the bam files in ES. accessionid: , captureset: ","source":"RecordService"}

```   

| HTTP METHOD | URI             		| USAGE     					  		            |
| ----------- | --------------- 		| --------------- 						            |
| POST        | /flim/accession       	| Display all ES records by the accessionids  	    | 

- Params
```javascript
{
  "accessions": ["CN-1800187"]
}

```

- Response      
```javascript
code: 200
{
    "result": "SUCCESS",
    "keyValueTable": [
        {
            "accession": "CN-1800187",
            "libraryid": "CN-CX251",
            "capture": "TS1Ev5",
            "run_folder": "PRD_180730_E00599_0096_AHLYLTCCXY"
        }
    ],
}

```   


### SAS Sample Cache
| HTTP METHOD | URI             		| USAGE     					  		                                            |
| ----------- | --------------- 		| --------------- 						                                            |
| GET         | /jsonCacheSample     	| Copy SAS delivery data and overwrite the last recently used sample cache folder 	| 

- Arguments
```
{
 	"accessionDatapath": "/path/to/accesion/folder",
 	"libraryDatapath": "/path/to/library/folder",
    "ext_id": "plm transaction id",
    "user": "username",
    "libraryid": "CX030",
    "accessionid": "18050432",
    "captureset": "wg-3323",
    "machineType": "xxxxx",
    "sampleType": "xxxxx",
    "state": "wait_xxxxxx",
    "eventId": "xxxxx"
}
```

- Response      
```javascript
code: 200
{
    "result": "SUCCESS",
    "files": {
        "message": "",
        "totalFilesSize": 0,
        "files": "xxx.zip|xxx.zip|xxx.zip"
    },
    "message": "Request queued"
}

```   


## Documentation
use [javadoc](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html) to generate a document. I order to make human readable documents, please use java document comment. To compile documents:

```
sbt doc
```

And then go to target/scala-2.12/api to see documents.


# end.

                      
