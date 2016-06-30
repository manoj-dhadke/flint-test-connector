# flint-test-connector
Demonstrates development of [Flint Connector](http://docs.getflint.io/getting_started/terminology#connectors) using Flint Software Development Kit.

## Requirements
In any Maven project, you will need to add **flint-sdk-1.0.0.1** dependency :

```
<repositories>
        <repository>
            <id>com.infiverve.flint.sdk</id>
            <name>flint-sdk</name>
            <url>http://sdkrepo.getflint.io.s3-website-us-east-1.amazonaws.com/release</url>
        </repository>
</repositories>
<dependency>
       <groupId>com.infiverve.flint.sdk</groupId>
       <scope>provided</scope>
       <artifactId>flint-sdk</artifactId>
       <version>1.0.0.1</version>
</dependency>
```

## Development
Writing a Connector is at your fingertips with Flint Software Development Kit, great for relying on IDE auto-completion to write code quickly. Here's an example:

```
import com.infiverve.flint.sdk.connectors.FlintConnectorBase;
import com.infiverve.flint.sdk.connectors.FlintConnectorRequest;
import com.infiverve.flint.sdk.connectors.exceptions.FlintConnectorException;
import io.vertx.core.json.JsonObject;

public class TestConnectorService extends FlintConnectorBase {

    @Override
    protected void disable() throws FlintConnectorException {

      /**
       * Your clean up code must go here, will be called while stopping flint
       */

    }

    @Override
    protected void enable(JsonObject config) throws FlintConnectorException {

      /**
       * process connector configuration data
       *
       */

    }

    @Override
    protected void onRequest(FlintConnectorRequest connectorRequest) {

     /**
      * methods available with FlintConnectorRequest
      */

      connectorRequest.getAction(); // connector action
      connectorRequest.getJobID(); // job-id of the request being submitted
      connectorRequest.getRequestJson(); // connector request parameters

     /**
      * different ways you can send back data to flint  
      */
      connectorRequest.sendResponse(new JsonObject().put("success", Boolean.TRUE));
      connectorRequest.sendResponse(-1, "required fields not present");
      connectorRequest.sendResponse(0, "success", new JsonObject().put("sum", "85"));


    }

}
```

You also need to create a json file and place it into _'**src/main/resources**'_ directory of your Maven project.

```
{
    "main": "com.infiverve.flint.connector.test.TestConnectorService", # class name which extends FlintConnectorBase of flint-sdk
    "options": {
        "worker": true
    }
}

```

**_Note: The name of json file should be same as project name_**

## Compile

By navigating into the directory of your Maven project, run command.

```
mvn clean install -Dmaven.test.skip=true

```
Upon compilation a fat jar will be created in _'**target**'_ directory of your Maven project like **flint-test-connector-1.0.0.0-fat.jar**

## Testing with Flint

1. Navigate to flint-x.x.x.x/connectors directory in Flint
2. Copy flint-test-connector-1.0.0.0-fat.jar recently created here
3. Restart Flint
4. [Add a Connector to the Grid](http://docs.getflint.io/grid_configuration/connectors#how-to-add-a-connector-to-the-grid)
