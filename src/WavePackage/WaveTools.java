/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WavePackage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//these imports are for file saving
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * WaveTools library for working with wave files and to use in MorsePlayer
 * library
 *
 * @author brian_000
 */
public class WaveTools {

    public static int sampleRate = 44100;
    public int channels = 1; //currently only supports on channel. Maybe Later 
    public double volume = 27040; // just set a volume for now.

    /**
     * combineByteArray simply combines the first and the second byte array as
     * one.
     *
     * @param firstByte First byte array desired
     * @param secondByte Second byte array to follow first
     * @return Returns firstByte plus secondByte
     */
    public static byte[] combineByteArray(byte[] firstByte, byte[] secondByte) {
        byte[] combinedArray = new byte[(firstByte.length + secondByte.length)];
        ByteBuffer bb = ByteBuffer.wrap(combinedArray);

        bb.position(0);
        bb.put(firstByte);
        bb.put(secondByte);

        return combinedArray;
    }

    /**
     * Saves a byte array as a file. Can be used to save wave files and PCM
     * files
     *
     * @param waveByteArray A byte array that you desire to save to a file.
     * @param filename The filename you wish the file to have.
     */
    public static void saveToWaveFile(byte[] waveByteArray, String filename) {
        FileOutputStream fop = null;
        File file;

        try {

            file = new File(filename);
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fop.write(waveByteArray);
            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
            }
        }

    }

    class PcmHeader {
        //int mSampleRate = 44100;
        //int mSampleRate = 16000;
        short mChannels = 1;
        //short mBitsPerSample = 16;
        String mWaveName;
        String mFileName = mWaveName + ".wav";
        //TODO this is for input into the createWaveHeaderForPcm sub!
        //TODO find out how to add a byte array to this !
    }

    /**
     * Used for writing Unsigned Integer to Byte Array.
     *
     * @param out
     * @param val
     */
    private static void writeInt(ByteBuffer buffer, int intToUnsign) {
//TODO add suppress warning here
        buffer.put((byte) (intToUnsign));
        buffer.put((byte) (intToUnsign >> 8));
        buffer.put((byte) (intToUnsign >> 16));
        buffer.put((byte) (intToUnsign >> 24));
        //out.write(val >> 8);
        //out.write(val >> 16);
        //out.write(val >> 24);
    }

    /**
     * This method accepts a byte array and creates and attaches a wave header
     * to the data.
     *
     * @param pcmData Pure PCM data as a byte []
     * @param sampleRate Sample rate of PCM data. Usually 44100
     * @param bitsPerSample Bit rate of PCM data. Usually 16
     * @return Returns Byte Array with newly created wave header and PCM wave
     * data attached.
     */
    public static byte[] createWaveHeaderForPcm(byte[] pcmData, int sampleRate, short bitsPerSample) {
//TODO something isnt working right in this!        
//Initialize variables for wave header
        short numberChannels = 1;  // number of channels
        int byteRate = sampleRate * numberChannels * bitsPerSample / 8;
        short blockAlign = (short) (numberChannels * bitsPerSample / 8);
        int subChunk2Size = pcmData.length;
        //
        int pcmLength = pcmData.length;
        int waveLength = pcmData.length + 44;
        waveLength++;
        //conduct check of wave file length
        if (waveLength % 2 != 0) {
            waveLength++;
            pcmLength++;
            pcmLength++;
        }  //wave files must have an even number of bytes!
        System.out.println("waveLength Value : " + waveLength);
        ByteBuffer waveBuffer = ByteBuffer.allocate(pcmLength + 44);
        //byte[] completeWave = new byte[pcmLength + 44];
        waveBuffer.position(0);
        //Set to Big Endian for RIFF
        waveBuffer.order(ByteOrder.BIG_ENDIAN);
        //write ASCII 'RIFF' to Byte Buffer
        waveBuffer.put((byte) 0x52);
        waveBuffer.put((byte) 0x49);
        waveBuffer.put((byte) 0x46);
        waveBuffer.put((byte) 0x46);
        //Set to Little Endian for ChunkSize
        waveBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //write Int Subchunk size
        waveBuffer.putInt(36 + subChunk2Size); //Should be calculated properly...This is jjust for testing
        //Set to Big Endian for WAVE
        waveBuffer.order(ByteOrder.BIG_ENDIAN);
        //write ASCII 'WAVE' to Byte Buffer
        //0x57415645 big-endian form
        byte[] asciiwave = {0x57, 0x41, 0x56, 0x45}; //wave in hex ASCII
        waveBuffer.put(asciiwave);
        byte[] asciifmt = {0x66, 0x6d, 0x74, 0x20};//  fmt in ASCII 0x666d7420 big-endian form).
        waveBuffer.put(asciifmt);
        //sub chunk size also bitrate = 16
        waveBuffer.order(ByteOrder.LITTLE_ENDIAN);
        waveBuffer.putInt(bitsPerSample);  //This is 16 for the bit-rate  ***WATCH FOR ERRORS HERE
        waveBuffer.putShort((short) 1);  //1 is for PCM 2   AudioFormat      PCM = 1 (i.e. Linear quantization)
        waveBuffer.putShort(numberChannels); //2   NumChannels      Mono = 1, Stereo = 2, etc.
        writeInt(waveBuffer, sampleRate);
        writeInt(waveBuffer, byteRate);        
        waveBuffer.putShort(blockAlign);//2   BlockAlign       == NumChannels * BitsPerSample/8
        waveBuffer.putShort(bitsPerSample);//2   BitsPerSample    8 bits = 8, 16 bits = 16, etc.
        //switch to big endian again
        waveBuffer.order(ByteOrder.BIG_ENDIAN);
        //Subchunk2ID      Contains the letters "data"
        byte[] asciidata = {0x64, 0x61, 0x74, 0x61};
        waveBuffer.put(asciidata);
        //switch back to little Endian
        waveBuffer.order(ByteOrder.LITTLE_ENDIAN);
        waveBuffer.putInt(subChunk2Size);
        // ADD PCM DATA TO HEADER
        waveBuffer.put(pcmData);
        //convert waveBuffer to byte[]
        byte[] completeWave = waveBuffer.array();
        return completeWave;
    }

    /**
     * Creates a smooth volume effect to a wave to prevent pops on play back.
     *
     * @param pcmData Byte Array of PCM data (16-bit only!)
     * @param fadeTime Fade Time is seconds
     * @param sampleRate Sample rate of PCM data Byte Array
     */
    public static void createHannWindow(byte[] pcmData, float fadeTime, int sampleRate) {

        ByteBuffer bb = ByteBuffer.wrap(pcmData);

        bb.asShortBuffer();
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // Calculate duration, in samples, of fade time
        double numFadeSamples = fadeTime * sampleRate;
        short sliceValue;

        for (int s = 0; s < numFadeSamples; s++) {
            // Calculate weight based on Hann 'raised cosine' window
            float weight = 0.5f * ((float) ((1 - ((float) Math.cos((float) Math.PI * (float) s / (float) (numFadeSamples))))));
            //Fade In
            sliceValue = bb.getShort(s * 2);
            bb.putShort((s * 2), (short) (weight * sliceValue));                       // Fade In
            //Fade Out
            sliceValue = bb.getShort((pcmData.length - 2 - (2 * s)));
            bb.putShort((pcmData.length - (s * 2) - 2), (short) (sliceValue * weight));  // Fade Out

        }

        //This code is in C for a hann window
        //http://www.labbookpages.co.uk/audio/wavGenFunc.html#tone
    }

    /**
     * Creates a sine wave PCM byte array
     *
     * @param freq Desired frequency in hertz
     * @param volume_percent percent of desired volume 0 to 100
     * @param duration_ms Duration of wave in milliseconds
     * @param sampleRate Sample rate of PCM Data
     * @return
     */
    public static byte[] createSinePCM(short freq, short duration_ms, short volume_percent, int sampleRate) //@TODO all these parameters should be changed to doubles.
    {

        double maxAmplitude_16bit = 32767;
        short waveAmptude_16bit = 28800; // test value of volume! 0 to 32767 value
        double calculate;
        double calcSlices = (double) duration_ms / 1000D * (double) sampleRate; 
        int numberSlices = (int) calcSlices;
        //set up ByteBuffer
        ByteBuffer bb = ByteBuffer.allocate((numberSlices * 2));
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.asShortBuffer();
        bb.position(0);
        //Loop for sine wave
        for (int step = 0; step < numberSlices; step++) {
            calculate = (Math.sin(freq * Math.PI * 2 * step / sampleRate) * waveAmptude_16bit);
            bb.putShort((short) (calculate));
        }
        byte[] bytePCMsine = bb.array();

        return bytePCMsine;

    }

    /**
     * Creates a wave format of silence for specified duration and sample rate
     *
     * @param duration_ms
     * @param sampleRate
     * @return An Output Stream is returned
     */
    public static byte[] createSilencePCM(double duration_ms, double sampleRate) {

        ByteBuffer bb = ByteBuffer.allocate((int) (duration_ms / 1000 * sampleRate) * 2);
        bb.asShortBuffer();
        bb.position(0);
        
        for (int slice = 0; slice < ((int) (duration_ms / 1000 * sampleRate)) - 2; slice++) {
            bb.putShort((short) 0);
        }
        byte[] bytePCMsilence;
        bytePCMsilence = bb.array();

        return bytePCMsilence;
    }

    /**
     * Mixes two PCM wave byte arrays into one track. UNTESTED!
     *
     * @param firstPCMwave
     * @param secondPCMwave
     * @return mixedPCM returns mixed byte array
     */
    public static byte[] mixTwoPCM(byte[] firstPCMwave, byte[] secondPCMwave) {

        int byteLength;

        if (firstPCMwave.length >= secondPCMwave.length) {
            byteLength = firstPCMwave.length;
        } else {
            byteLength = secondPCMwave.length;
        }

        byte[] mixedPCM = new byte[byteLength];
        ByteBuffer bb = ByteBuffer.wrap(mixedPCM);

        for (int step = 0; step < byteLength; step++) {
            short slice = (short) ((firstPCMwave[step] / 2) + (secondPCMwave[step] / 2));
            bb.putShort(slice);
        }
        return mixedPCM;
    }

    public static void createWhiteNoise(byte[] noiseByteA) {
        //TODO Create sub function for white noise PCM data
    }

    public static void setSampleRate(int newSampleRate) {

        sampleRate = newSampleRate;
    }

    public static int getSampleRate() {

        return sampleRate;

    }

    /**
     * DONT USE! Just for testing! Main can be deleted after testing!
     *
     * @param args No command line arguments available in class.
     */
    public static void main(String[] args) {

// TODO code application logic here
        byte[] testWave;
        byte[] pcmWave;
        byte[] waveWithHeader;
        OutputStream fullWave;

        testWave = createSilencePCM(1000, 44100); // create one second of PCM silence....
        pcmWave = createSilencePCM(1000, 44100);
        waveWithHeader = createWaveHeaderForPcm(pcmWave, 44100, (short) 16);

        System.out.println(waveWithHeader.length);
        InputStream inputWave = new ByteArrayInputStream(waveWithHeader);
        boolean support = inputWave.markSupported();
        System.out.println(support);
        System.out.println(inputWave.getClass());
        System.out.println(inputWave.toString());

//create PCM
        byte[] testPCMsine;
        testPCMsine = createSinePCM((short) 1000, (short) 1000, (short) 1000, 44100);

//Add wave Header to PCM
        byte[] testWaveWithHeader;
        testWaveWithHeader = createWaveHeaderForPcm(testPCMsine, 44100, (short) 16);
        System.out.println(testWaveWithHeader.length);
        saveToWaveFile(testPCMsine, "testpcm.pcm");  // pcm data looks beautiful in audacity!
        saveToWaveFile(testWaveWithHeader, "testwave.wav"); // wave wont load :( in Audacity
        createHannWindow(testPCMsine, 0.005F, 44100);
        saveToWaveFile(testPCMsine, "pcmWithHann.pcm");

        byte[] ditByte;
        byte[] dahByte;
        byte[] interSpace;
        byte[] characterSPace;
        byte[] finishByte;// = null;
        byte[] workingByte;
        ditByte = createSinePCM((short) 1000, (short) 100, (short) 0, 44100);
        createHannWindow(ditByte, 0.005F, sampleRate);
        dahByte = createSinePCM((short) 1000, (short) 300, (short) 0, 44100);
        createHannWindow(dahByte, 0.005F, sampleRate);
        interSpace = createSilencePCM(100, 44100);
        characterSPace = createSilencePCM(700, sampleRate);

        workingByte = combineByteArray(ditByte, interSpace);
        finishByte = combineByteArray(workingByte, dahByte);
        workingByte = combineByteArray(finishByte, interSpace);

        saveToWaveFile(workingByte, "combined.pcm");
        byte[] waveToSave;
        waveToSave = createWaveHeaderForPcm(workingByte, sampleRate, (short) 16);
        saveToWaveFile(waveToSave, "combined.wav");

        long unsignedInt = 44100;
        unsignedInt = (unsignedInt * 2) & 0xffffffff;
        System.out.println(unsignedInt);
        System.out.println((int) unsignedInt);
        ByteBuffer pooptester = ByteBuffer.allocate(32);
        pooptester.putLong((int) unsignedInt);


    }

}
