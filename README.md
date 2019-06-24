### Overview
This program takes pdf input and translates from any language supported by Microsoft Translate, to English. The translations are done on a word for word basis, for x amount of words with length greater than or equal to y (these values are entered by the user). The same word is not considered twice and words that are less common are given priority.

The output is a pdf with 2 columns and 45 rows per page. Once the program closes, the output PDF should be complete.

### Dependencies

- Java JRE/JDK 6 or later (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 - Once installed, add java to your path
   - Navigate Control Panel to "System and Security", "System", "Advanced System Settings", "Advanced" tab, "Environment Variables"
   - You should have a variable named "Path", if not, create one
   - Select "edit" then "new"
   - Include the java path
     - The path should look like this "C:\Program Files (x86)\Common Files\Oracle\Java\javapath"
- Microsoft Azure
 - Create a Microsoft Azure account (https://azure.microsoft.com/en-us/)
 - Go to your Azure portal and select "Create a resource"
 - Search for Translate Text and create the resource
 
### Instructions
 - Run the jar file and select the input/outputs
  - The input must be a pdf and the output location should be a folder
  - The output file name should not include any file extension - it will be made a pdf
  - You are provided two api keys - which can be regenerated - with your translator resource on your Azure account
 - 2,000,000 characters can be translated for free per month. After the limit, the translations will not occur unless you upgrade to a paid subscription
  - You may view how many characters you have remaining on your Azure account in the overview for your translator resource
 - the config file is used to fill the fields with their previously entered information
  - do not edit, delete, or rename it