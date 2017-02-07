# AndroidBeacon
A simple demo to implement Neighbor Discovery on Android devices.

## Introduction
Neighbor discovery plays a crucial role in the formation of wireless sensor networks and mobile networks where the power of sensors (or mobile devices) is constrained.

I implemented Hedis, Todis and other reference protocols list below on Xiaomi Mi Note. Mi Note phone, a smartphone by Xiaomi that support Bluetooth Low Energy(BLE). All smartphone are based on Android 6.0.1.
You also can try this demo on other phone as you like with some small change.

## Implemented protocol 
#####Disco (Sensys 2008)
paper: **Practical Asynchronous Neighbor Discoveryand Rendezvous for Mobile Sensing Applications**

#####U-Connect (IPSN 2010)
paper: **U-Connect: A Low-Latency Energy-Efficient AsynchronousNeighbor Discovery Protocol**

#####Searchlight (MobiCom 2011)
paper: **Searchlight: Won’t you bemy neighbor?**

#####HEDIS and TODIS (INFOCOM 2015)
 paper: **On heterogeneous neighbor discovery in wireless sensor networks**

## Usage

1. Set uuid to differentiate phones.
2. Choose which protocol by modify variable `type`
3. Set the duty cycle.
4. Test the BLE transition latency from sleep to transmit/receive (You should write code about pre slot by yourself like Searchlight if the latency is long in your phone).
5. Run on your phone and get the result.

If you have any questions, please contact me at nikefd@gmail.com.
