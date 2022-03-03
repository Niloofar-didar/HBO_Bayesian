
Welcome to the Mixed AR-AI project. This projects is aimed at improving the performance of AI and AR by controlling the objects triangle count that affects the overall GPU usage of the mobile phone to leave enough space for both modules to run their tasks on GPU.
You need to have android studio installed on your system. Then, you can download and install the android app on your phone. The app consists of two modules, AR and AI. In the AI module, some AI tasks are running some inferences on input of camera frames. To get it run, please first make sure to store chair.jpg in the local storage of your phone in this directory : Storage/emulated/0 
one easy way to do it is just to copy and paste the image to the main (home) directory of your phone.
check the orange box in the left side of the app menu. First select the AI request number which is visible as Tasks. in the middle column you could switch between the AI models, and in the last column, you can select either of devices for inference. (CPU, GPU, or NNAPI). The results of AI module is stored in response_t.csv at the local storage of your phone, following the project direction : Storage/emulated/0/Android/data/com.arcore.MixedAIAR/files
We can observe that the inference performance is better while using GPU.
To use the AR modeule, you can select the virtual objects from the menu and put them on the screen. The result for AR module, i.e., the overall triangle count of the objects and the GPU usage is stored at GPU_Usage.cvs.
The current version of app uses the decimated objects from the locall storage. so you need to have the decimated version of all the used virtual objects on your phone at the same directory. (Storage/emulated/0/Android/data/com.arcore.MixedAIAR/files).
To generate the decimated version of the objects you could use the python program provided in "Files related to decimating and convering OBJ to SFB" folder of the project and follow the guide from that directory. 
you could see in below a screenshot of the app.

![Screenshot_20220302-195639](https://user-images.githubusercontent.com/27611369/156475621-3106af4e-08d0-4511-8287-43605abfe6e4.jpg)
