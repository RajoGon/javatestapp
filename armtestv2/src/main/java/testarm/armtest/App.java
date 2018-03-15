package testarm.armtest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
/**
 * Hello world!
 *
 */
public class App 
{

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean pattern1(Azure azure) {
        final String rgName = "RG-tshirt-20";
        String deploymentName = null;
        try {
        	
			
            //=============================================================
            // Create resource group.

            System.out.println("Creating a resource group with name: " + rgName);

            azure.resourceGroups().define(rgName)
                    .withRegion(Region.US_EAST)
                    .create();

            System.out.println("Created a resource group with name: " + rgName);


            //=============================================================
            // Create a deployment for an Azure App Service via an ARM
        	
        	
        	 String templateJson=null;
        	 //reading different templates, based on the patterns provided. Dependencies can be managed here.
        	for(int i=0;i<2;i++){
        		if(i==0) {
        			deploymentName = SdkContext.randomResourceName("dpRSAT",24);
        			templateJson = App.getTemplateForServer();
        		}
        		if(i==1) {
        		    deploymentName = SdkContext.randomResourceName("dpRSAT",24);
        			templateJson = App.getTemplateForDb();
        		}
			           
			
			            System.out.println(templateJson);

			            //Starting the deployment for the different resources.
			
			            System.out.println("Starting a deployment for "+rgName+" with deployment name : " + deploymentName);
			
			            azure.deployments().define(deploymentName)
			                    .withExistingResourceGroup(rgName)
			                    .withTemplate(templateJson)
			                    .withParameters("{}")
			                    .withMode(DeploymentMode.INCREMENTAL)
			                    .create();
			
			            System.out.println("Started a deployment with managed disks: " + deploymentName);
			            Deployment deployment = azure.deployments().getByResourceGroup(rgName, deploymentName);
			            System.out.println("Current deployment status : " + deployment.provisioningState());
			            
			            while (!(deployment.provisioningState().equalsIgnoreCase("Succeeded")
			                    || deployment.provisioningState().equalsIgnoreCase("Failed")
			                    || deployment.provisioningState().equalsIgnoreCase("Cancelled"))) {
			                SdkContext.sleep(10000);                
			                deployment = azure.deployments().getByResourceGroup(rgName, deploymentName);
			                System.out.println("Current deployment status : " + deployment.provisioningState());
			            }
			           //System.out.println("Name : "+deployment.manager().genericResources().listByResourceGroup(rgName).get(i).name()); 		
			           //System.out.println("Location : "+deployment.manager().genericResources().listByResourceGroup(rgName).get(i).region());
			           //System.out.println("Type : "+deployment.manager().genericResources().listByResourceGroup(rgName).get(i).type());
            
        }
            return true;
            
            
            
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
/*        	Deployment deployment = azure.deployments().getByResourceGroup(rgName, deploymentName);;
        	for(int i=0;i<2;i++) {
				Map<?, ?> properties = (Map<?, ?>) deployment.manager().genericResources().listByResourceGroup(rgName).get(i).properties();
                System.out.println("******************Properties for ***************************");
                
			                
    			 for( Entry<?, ?> p : properties.entrySet()) {
    				 System.out.println(p.getKey() + " = " + p.getValue());									 
    			 }
    			 
        	}
*/
			 
          /*  try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }*/
        }
        return false;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
    	
    	//read DB
    	
    	//
    	
    	
    	
    	
        try {

    		//Set up the basic authentication
    		final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
			Azure azure = Azure.configure()
			        .withLogLevel(LogLevel.BASIC)
			        .authenticate(credFile)
			        .withDefaultSubscription();
			// Auth done with the azure service, only then we can go ahead with other functions
			// Different patterns are called depending on the database entry.
            pattern1(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getTemplateForServer() throws IllegalAccessException, JsonProcessingException, IOException {
    	System.out.println("Deploying server");
        final String myserverAdminLogin = "rajoserverlogin";
        File  f  = new File("C:/Users/gon_r/Desktop/backs/Powershell/repos/RG-tshirt/RG-tshirt/nestedtemplates/sqlserver.json") ;
        final InputStream embeddedTemplate;
        embeddedTemplate = new FileInputStream(f);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode tmp = mapper.readTree(embeddedTemplate);
        App.validateAndAddFieldValue("string", myserverAdminLogin, "myserverAdminLogin", null, tmp);
        return tmp.toString();
    }
    
    
    private static String getTemplateForDb() throws IllegalAccessException, JsonProcessingException, IOException {
    	System.out.println("Deploying Db");
        final String tshirt = "small";
        String databasesize;
        if(tshirt.equals("small")) {
        	databasesize="1073741824";
        }else {
        	databasesize="2147483648";
        }       
        File  f  = new File("C:/Users/gon_r/Desktop/backs/Powershell/repos/RG-tshirt/RG-tshirt/nestedtemplates/mydb.json") ;
        final InputStream embeddedTemplate;
        embeddedTemplate = new FileInputStream(f);
        System.out.println("Template = "+embeddedTemplate);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode tmp = mapper.readTree(embeddedTemplate);    
        App.validateAndAddFieldValue("string", databasesize, "databasesize", null, tmp);
        return tmp.toString();
    }
    

    private static void validateAndAddFieldValue(String type, String fieldValue, String fieldName, String errorMessage,
                                                 JsonNode tmp) throws IllegalAccessException {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode parameter = mapper.createObjectNode();
        parameter.put("type", type);
        if (type == "int") {
            parameter.put("defaultValue", Integer.parseInt(fieldValue));
        } else {
            parameter.put("defaultValue", fieldValue);
        }
        ObjectNode.class.cast(tmp.get("parameters")).replace(fieldName, parameter);
    }
}
