package com.fabrikam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.DeploymentOperation;
import com.microsoft.azure.management.resources.Feature;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroupProperties;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

import org.json.*;
/**
 * Hello world!
 *
 */

class Resource{
	public Resource() {
		super();
	}
	String name;
	String id="hi";
	String type;

	String resourceGroupName;
	String resourceGroupId;
	String location;
	String subscriptionId;
	Object tags;
	String state;
	Boolean inserted=false;
	Boolean propertiesInserted = false;
	
	public Boolean getPropertiesInserted() {
		return propertiesInserted;
	}
	public void setPropertiesInserted(Boolean propertiesInserted) {
		this.propertiesInserted = propertiesInserted;
	}
	int rowId=0;
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public Boolean getInserted() {
		return inserted;
	}
	public void setInserted(Boolean inserted) {
		this.inserted = inserted;
	}
	Map<?, ?> properties;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getResourceGroupName() {
		return resourceGroupName;
	}
	public void setResourceGroupName(String resourceGroupName) {
		this.resourceGroupName = resourceGroupName;
	}
	public String getResourceGroupId() {
		return resourceGroupId;
	}
	public void setResourceGroupId(String resourceGroupId) {
		this.resourceGroupId = resourceGroupId;
	}
	@Override
	public String toString() {
		return "Resource [name=" + name + ", id=" + id + ", type=" + type + ", resourceGroupName=" + resourceGroupName
				+ ", resourceGroupId=" + resourceGroupId + ", location=" + location + ", subscriptionId="
				+ subscriptionId + ", tags=" + tags + ", state=" + state + ", inserted=" + inserted + ", properties="
				+ properties + "]";
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getSubscriptionId() {
		return subscriptionId;
	}
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	public Object getTags() {
		return tags;
	}
	public void setTags(Object tags) {
		this.tags = tags;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Map<?, ?> getProperties() {
		return properties;
	}
	public void setProperties(Map<?, ?> properties) {
		this.properties = properties;
	}
	public Resource(String name, String id, String type, String resourceGroupName, String resourceGroupId,
			String location, String subscriptionId, Object tags, String state, Map<?, ?> properties) {
		super();
		this.name = name;
		this.id = id;
		this.type = type;
		this.resourceGroupName = resourceGroupName;
		this.resourceGroupId = resourceGroupId;
		this.location = location;
		this.subscriptionId = subscriptionId;
		this.tags = tags;
		this.state = state;
		this.properties = properties;
	}
	
}



public class App 
{
	static Boolean inserted = false; 
	static int readyToInsert = 0;
	public static String insertResources(Connection connection, Azure azure, String rgName, String rgId) throws InterruptedException {
		

		String failedResource="No Failures";
		int totalResources = azure.deployments().listByResourceGroup(rgName).size();
		totalResources--;
		System.out.println("Total resources = "+totalResources);		 
		PagedList<GenericResource> z = azure.genericResources().listByResourceGroup(rgName);		
										
		while(z.isEmpty()) {
			Thread.sleep(7000);
			z = azure.genericResources().listByResourceGroup(rgName);
			
		}
		System.out.println("Total resources from generic = "+z.size());	
		 totalResources = z.size();
        Resource[] r = new Resource[10];
		while(readyToInsert < z.size() ) {
			readyToInsert = 0;
			z = azure.genericResources().listByResourceGroup(rgName);
			for(int i =0 ;i<z.size();i++) {
				GenericResource temp = z.get(i);
				if(!temp.id().isEmpty()) {
					readyToInsert ++;
				}else {

					System.out.println("no id");
				}
			}
			System.out.println("Waiting to insert");
		}
		
		System.out.println("Preparing to insert into Database");
		
		
        

		while(true){
			Thread.sleep(3000);
			
			z = azure.genericResources().listByResourceGroup(rgName);
			System.out.println("Total resources"+z.size());
            for(int i =0 ;i<z.size();i++) {
            	GenericResource temp = z.get(i);
            	
            		if(r[i]==null) {
            			r[i] = new Resource();      		
                		r[i].setId(temp.id());
                    	r[i].setName(temp.name());
                    	r[i].setLocation(temp.regionName());
                    	r[i].setType(temp.type());
                    	r[i].setProperties((LinkedHashMap<?, ?>) temp.manager().genericResources().getById(temp.id()).properties());
                    	r[i].setResourceGroupId(rgId);
                    	r[i].setSubscriptionId(azure.subscriptionId());
                    	r[i].setResourceGroupName(rgName);
                    	r[i].setState(azure.deployments().getById(temp.id()).provisioningState());
                    	r[i].setTags(temp.tags());
                    	
                    	String id = r[i].getId();
                    	String name = r[i].getName();
                    	String location = r[i].getLocation();
                    	String type = r[i].getType();
                    	Map<?, ?> properties = r[i].getProperties();
                    	String recourceGroupId = r[i].getResourceGroupId();
                    	String subId = r[i].getSubscriptionId();
                    	String resourceGroupName = r[i].getResourceGroupName();
                    	String state = r[i].getState();
                    	String tag = null;
                    	
        	            String selectSql =  "INSERT INTO dbo.Resources(ResourceName,ResourceId,ResourceType,ResourceGroupName,ResourceGroupId,Location,SubscriptionId,Tags,ProvisioningState,ResourceDeploymentName,Properties) OUTPUT INSERTED.id VALUES (?,?,? ,?,?,?,?,?,?,?,?)";
        	            PreparedStatement p;
						try {
							p = connection.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
							p.setString(1, name);
	        	            p.setString(2, id);
	        	            p.setString(3, type);
	        	            p.setString(4, resourceGroupName);
	        	            p.setString(5, recourceGroupId);
	        	            p.setString(6, location);
	        	            p.setString(7, subId);
	        	            p.setNString(8,tag);
	        	            p.setString(9, state);
	        	            p.setString(10, name);
	        	            p.setString(11, "null");
	        	            p.executeQuery();
	        	            
	        	            ResultSet rs = p.getGeneratedKeys();
	        	            if(rs.next())
	                        {
	                            int last_inserted_id = rs.getInt(1);
	                            r[i].setRowId(last_inserted_id);
	                            System.out.println("Inserted at "+last_inserted_id);
	                        }
	        	            
	        	            r[i].inserted = true;
	        	            p.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        	            	
            		}else {
            			System.out.println("Updating"+r[i].getName());
            			r[i].setState(azure.deployments().getById(temp.id()).provisioningState());
            			String state = r[i].getState(); 
            			if(state.equals("Failed")) {
            				failedResource = r[i].getName();
            			}
            			String updateSql =  "UPDATE dbo.Resources set ProvisioningState=? where id=?";
        	            PreparedStatement p2;
						try {
							p2 = connection.prepareStatement(updateSql);
							p2.setString(1, state);
		        	        p2.setInt(2, r[i].getRowId());
		        	        p2.executeUpdate();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        	           
						if(!r[i].getProperties().isEmpty() && r[i].getPropertiesInserted()==false) {
				
							
							 String propertiesSql =  "INSERT INTO dbo.test VALUES (?,?,?)";
							 PreparedStatement p3;
	
							 
							 for( Entry<?, ?> p : r[i].getProperties().entrySet()) {
								 try {
										p3 = connection.prepareStatement(propertiesSql);
										p3.setInt(1, r[i].getRowId());
										p3.setObject(2, p.getKey());
										p3.setObject(3, p.getValue());
										p3.executeUpdate();
										r[i].setPropertiesInserted(true);
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}  
									 
							 }
							 
							
							 
						}
            		}
            		
            		
       	           
            }
            //looping
            if(azure.deployments().listByResourceGroup(rgName).get((azure.deployments().listByResourceGroup(rgName).size())-1).provisioningState().equals("Succeeded")||azure.deployments().listByResourceGroup(rgName).get((azure.deployments().listByResourceGroup(rgName).size())-1).provisioningState().equals("Failed") ) {
            	
            	if(!failedResource.equals("No Failures")) {
            		rgName = rgName + "F";
            	}
            	
            	
            	String updateFinal =  "UPDATE dbo.ResourceGroups set InternalResourceDeploymentStatus=?,FailedDueTo=? where ResourceGroupName=? and id=?";
            	PreparedStatement p3;
				try {
					p3 = connection.prepareStatement(updateFinal);
					p3.setString(1, azure.deployments().listByResourceGroup(rgName).get((azure.deployments().listByResourceGroup(rgName).size())-1).provisioningState());
					p3.setString(2, failedResource);
					p3.setString(3, rgName);
					p3.setString(4, rgId);
					
        	        p3.executeUpdate();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				failedResource="No Failures";
				readyToInsert = 0;
				r=null;
				return azure.deployments().listByResourceGroup(rgName).get((azure.deployments().listByResourceGroup(rgName).size())-1).provisioningState();

            }
           
		}
	}
	
	
	
    public static void main( String[] args ) throws InterruptedException
    {
    	
    
    	
    	String hostName = "rajoserver.database.windows.net";
        String dbName = "rajodb";
        String user = "rajorshi";
        String password = "Qwertyuiop@123";
        String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", hostName, dbName, user, password);
        Connection connection = null;
    	
    	
		        try {
		            connection = DriverManager.getConnection(url);
		            String schema = connection.getSchema();
		            System.out.println("Successful connection - Schema: " + schema);
		          while(true)  {
		        	  Thread.sleep(3000);
		            // Create and execute a SELECT SQL statement.
		            String selectSql = "SELECT TOP 1 ResourceGroupID,ResourceGroupName,InternalResourceDeploymentStatus,id from dbo.ResourceGroups where InternalResourceDeploymentStatus='Pending' ORDER BY ID DESC";
		
		            try (Statement statement = connection.createStatement();
		                ResultSet resultSet = statement.executeQuery(selectSql)) {
		
		                    // Print results from select statement
		                    
		                    while (resultSet.next())
		                    {
		                    	System.out.println("Deploying : "+resultSet.getString(2));
		                    	//while(true) {
		                    		
		                        	Thread.sleep(3000);
		                        	try {
		                        		//Set up the basic authentication
		                        		final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
		                    			Azure azure = Azure.configure()
		                    			        .withLogLevel(LogLevel.BASIC)
		                    			        .authenticate(credFile)
		                    			        .withDefaultSubscription();
		                    			// Auth done
		                    			
		                    			if(resultSet.getString(3).equals("Pending")) {
		                    				System.out.println("Deployment : Pending");
		                    		        System.out.println("***********************************************************");
		                    		        
		                    		        if(inserted==false) {
		                    		        	String status = insertResources(connection,azure, resultSet.getString(2),resultSet.getString(4));
		                    		        	System.out.println("Deployment is over with status : " + status);
		                    		        	inserted = true;
		                    		        }
		                    			
		                    			inserted=false;
		                    		        
		                    			}else {
		                    				System.out.println("Deployment for :"+resultSet.getString(2)+ " is already complete.");
		                    			}
		                    			//Delete function
		                    				
		                    			
		                    			//
		                    			
		                    				
		                    			} catch (CloudException e) {
		                    				System.out.println("Waiting");
		                    			} catch (IOException e) {
		                    				System.out.println("Waiting");
		                    			}
		                        	//}

		                    }
		             
		            }  catch (Exception e) {
			           System.out.println("Nothing to deploy");			            
				    }finally {
				    	 System.out.println("Nothing to deploy");		
				    }
		            
		            //loop here
		            
		        }
		    }
		    catch (Exception e) {
		            e.printStackTrace();
		            
		    }finally {
		    	try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }

    }
    	
}
