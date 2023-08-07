## Getting started for Windows Users
Before cloning the project, we need to create a `.gitconfig` file.
1. Open a terminal or command prompt/git bash and execute: `git config --global -e`
2. You should see a file appear inside the prompt window:
```
       [user]
               name = user name
               email = useremail@gmail.com
```
3. Close the window and cancel any operation.

   Navigate to:
    1. Go to directory C:\Users\UserName
    2. Open the file `.gitconfig` in any text editor
    3. Add the following line to the end of the script and uncomment by deleting the # symbol:
       ```
           [core]
                   autocrlf = input
       ```
       The file should look like the following:

       ```
           [user]
                   name = user name
                   email = useremail@gmail.com
           [core]
                   autocrlf = input
       ```
       NOTE: These steps should be done before cloning the project as the `core` element is essential.
       If the project has already been cloned, it should be deleted and cloned again with `core` setting applied.


4. Install WSL2 and Ubuntu Terminal (You can follow this [tutorial](https://ubuntu.com/tutorials/install-ubuntu-on-wsl2-on-windows-10#2-install-wsl) or follow these steps)
    1. Open Windows Powershell OR Command Prompt (If you need permissions then open as admin)
    2. Install WSL for the first time
       ```
       wsl --install
       ```
    3. Run to check the version
       ```
       wsl -l -v
       ```
    4. Then Run (depending on versions. need to check which versions are in version 1 and se to version 2)
       ```
       wsl --set-default-version 2
       ```
    5. Then Run
       ```
       wsl --install -d Ubuntu
       ``` 
    6. Once Ubuntu is installed your command prompt should look like this:
          ```
          computername@1234:/mnt/c/Users/UserName$
          ``` 
    7. To access the wsl (Ubuntu installed) open command prompt and use:
          ```
          wsl
          ```
    8. Close down command prompt.


5. Once installed WSL2 and Ubuntu Terminal is installed,
   open command prompt and Run the following commands:
      ```      
      wsl (this should open Ubuntu terminal inside command prompt)
      sudo apt update
      sudo apt upgrade
      sudo apt install bpython
      bpython
      ```

6. Now we need to configure JAVA_HOME:
   run in order (inside the Ubuntu terminal)

   ```
   sudo apt install openjdk-17-jdk
   sudo apt update
   ```
7. Now we need to configure JAVA_HOME variable
   ```
   nano ~/.bashrc
   ```
8. The command above will open a file. Use the down arrow key and navigate to the last line of the file.

9. Add the following at the end of the script:
   ```
   JAVA_HOME=$(dirname $( readlink -f $(which java) ))
   JAVA_HOME=$(realpath "$JAVA_HOME"/../)
   export JAVA_HOME
   ```
10. Write Out the file with the shortcut provided and then exit.
   ```
   sudo update-alternatives --config java
   sudo apt update
   ```
11. Use this command to check if JAVA17 is installed
   ```
   java -version   
   ```
12. You should be presented with this:
   ```
   openjdk version "17.0.7" 2023-04-18
   OpenJDK Runtime Environment (build 17.0.7+7-Ubuntu-0ubuntu122.04.2)
   OpenJDK 64-Bit Server VM (build 17.0.7+7-Ubuntu-0ubuntu122.04.2, mixed mode, sharing)
   ```

13. To install unzip run in the Ubuntu Terminal
    ```
    sudo apt-get install unzip
    ```
14. To install postgresql in the Ubuntu Terminal:
    ```
    sudo apt install postgresql postgresql-contrib
    ```

15. WSL needs to be enabled in docker:
    1. Open docker desktop > settings > resources > WSL INTEGRATION
    2. Tick the box where it says "Ubuntu" or the name of your ubuntu terminal

16. Use the IntelliJ terminal to run the project use the `wsl`.

### Troubleshooting:
[Microsoft Docs for installing Linux on Windows](https://learn.microsoft.com/en-us/windows/wsl/install)

[Error code 0x80070520](https://www.majorgeeks.com/content/page/microsoft_store_error_0x80070520.html)

Update WSL version: `wsl --update --web-download`

[Windows Subsystem for Linux Documentation](https://learn.microsoft.com/en-us/windows/wsl/)

[Other WSL related issues](https://learn.microsoft.com/en-us/windows/wsl/troubleshooting)