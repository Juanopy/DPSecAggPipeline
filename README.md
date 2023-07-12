# Distributed Secure DP Pipeline 

## Setting up the secure aggregation server.
- [ ] first you need to download and compile crypto++, so that we can include the resulting header-files into the project afterwards.
- [ ] next you need to download and compile boost, so that we may include this as well.

## Setting up the webserver (found in `asc-dl-pipeline`).

To set up the webserver and interface for yourself, please follow the steps below:

- [ ] install node.js version 19.1.0 as it is specified in the nvmrc.
- [ ] install all dependencies of the project using `npm install` in the `webserver` and `react-web` directories.
- [ ] install `yarn` on your system, because it is required by `webserver/start.sh`.
- [ ] set the PRIVEE_PATH-environment variable to the directory of the smartphone demo (for example `/asc-dl-pipeline`). You can also do this by editing `webserver/start.sh`. There you can find the command for setting this variable and simply add your custom path to it. 
- [ ] install the python package`numpy`.

### Optional steps in case of an error.
- [ ] Sometimes you need to make the `webserver/start.sh` and `webserver/stop.sh` executable first.
- [ ] In some cases your `nvm` isn't stored in the location the script expects it to be. In those cases you might want to look in `~/.nvm` for your local configuration.

## After installing the webserver and webinterface.

Now we just need to start up the server. To do this we just execute the `webserver/start.sh` and using `npm start` in the `webserver/react-web` directory in another terminal window. This is because the current implementation relies on two servers: one for gathering the data and doing the calculation and the other one to display the data on a website.

## Setting up the app.

Once we have the webserver and webinterface up and running, we can continue by setting up the app. To do this, just follow these steps:

- [ ] Import the project "app" into Android studio.
- [ ] Create a device with the target sdk 31.
- [ ] Adjust the RAM of the simulated device to around 4 GB and the internal storage to around 20 GB, so you won't crash, because of unsufficent storage size.
- [ ] change the url in `clustering/KMeans:159` to the url you set in our webserver (if you changed it, else it is set to localhost).


### Optional steps in case of error.

- [ ] Make sure you're gradle, android studio and plugins are updated, because this could also lead to some troubles while setting up the app. 
- [ ] If CMake is not found, you might want to install the required version 3.10.2 directly in Android studio.

## Additional information regarding the app.
We also included some files you could use for secure aggregation in `app_extensions`. 
You can implement those in your app to complete the protocol with our secure aggregation server.

