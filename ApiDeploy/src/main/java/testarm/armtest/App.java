package testarm.armtest;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;


public class App 
{    
		public static boolean powershellvm(Azure azure) {
        final String rgName = "initialsetupgroup9";
        final String powerShellWindowsInstallScript = "https://raw.githubusercontent.com/RajoGon/installPowerShell/master/test.ps1";
        final String installPowerShellWindowsCommand = "powershell.exe -ExecutionPolicy Unrestricted -File test.ps1";
        final List<String> windowsScriptFileUris = new ArrayList<String>();
        windowsScriptFileUris.add(powerShellWindowsInstallScript);
        try {
            //=============================================================
            // Create resource group.

            System.out.println("Creating a resource group with name: " + rgName);

            azure.resourceGroups().define(rgName)
                    .withRegion(Region.US_EAST)
                    .create();

            System.out.println("Created a resource group with name: " + rgName);


            //=============================================================      	
        	 //reading different templates, based on the patterns provided. Dependencies can be managed here.
            System.out.println("Creating the PowerShell VM");
        	VirtualMachine newPowerShellVm = azure.virtualMachines().define("PowerShellVm")
        		            .withRegion(Region.US_EAST)
        		            .withNewResourceGroup(rgName)
        		            .withNewPrimaryNetwork("10.5.0.0/28")
        		            .withPrimaryPrivateIPAddressDynamic()
        		            .withNewPrimaryPublicIPAddress("powerShellIP9")
        		            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
        		            .withAdminUsername("Azuretestjavavm")
        		            .withAdminPassword("Newuser@1234567")     		            
	                        .create();

        	System.out.println("Successfully Deployed the PowerShellVM. Find the Details Bellow : -");
        	System.out.println("Vm Name - "+newPowerShellVm.computerName());
        	System.out.println("Now running scripts...");
        	newPowerShellVm.update()
	            .defineNewExtension("CustomScriptExtension")
	            .withPublisher("Microsoft.Compute")
	            .withType("CustomScriptExtension")
	            .withVersion("1.9")
	            .withMinorVersionAutoUpgrade()
	            .withPublicSetting("fileUris", windowsScriptFileUris)
	            .withPublicSetting("commandToExecute", installPowerShellWindowsCommand)
	            .attach()
            .apply();


        	return true;   
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
  
        }
        return false;
    }


    public static void main(String[] args) {   	
    	
        try {

    		//Set up the basic authentication
        	String subscriptionID = "9e9fc30f-5ba2-45a7-8448-6f899b3f5122";
        	ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
        			"http://testjavaapp", "39a6093c-5919-4044-adfc-55d19ced526b", "java123", AzureEnvironment.AZURE);
        	Azure azure = Azure.authenticate(credentials).withSubscription(subscriptionID);
            powershellvm(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
