# CSC509-FinalProject: Reading &amp; Emotion Tracker

## Project Overview
This project is a prototype for an application to help users read more efficiently. 
It uses mouse data to simulate eye tracking and an emotion-tracking headset (real or virtual)
to help users visualize emotion patterns while reading in real-time. The application
highlights text to signify the main emotion while reading that section and shows the 
frequency of each emotion.

## Emotiv Launcher
In order to get emotion information (real or simulated), you will need to install the free
[EMOTIV Launcher software](https://www.emotiv.com/products/emotiv-launcher). You will need to 
make an account if you don't already have one. Once signed in you can create a virtual Emotiv
device if you do not have access to a real one (we use the Insight model 1.0).

## How to Run the Application
1. Open the [EMOTIV Launcher](#emotiv-launcher) application.
2. Power on the Emotiv device (either the actual device or the virtual one.)
3. Connect to the Emotiv device.
4. ** (If using a real device) put on the device, paying attention to the prompts.
5. Run this application.
6. ** If it is your first time running the application, a pop-up should appear in the EMOTIV Launcher window asking to approve the application for accessing the software. Click Approve. Then close and restart this application.
7. You can begin data visualization by clicking 'Actions' -> 'Start'.
8. Wait for the pop-up showing a successful connection the mqtt broker. 
9. You can then simulate your gaze by dragging the mouse across the text you are reading. 