package models;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class VideoProcessor {
    public String processVideo(String filenamevideo, String filenameaudio, String outputPath)
    {
        IMediaWriter mWriter = ToolFactory.makeWriter(outputPath); //output file


        /* OLD CODE
        IContainer containerVideo = IContainer.make();
        IContainer containerAudio = IContainer.make();

        if (containerVideo.open(filenamevideo, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + filenamevideo);

        if (containerAudio.open(filenameaudio, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + filenameaudio);*/

        // NEW CODE

        InputStream videoInputStream = null;
        InputStream audioInputStream = null;
        try {
            videoInputStream = new FileInputStream(new File(filenamevideo));
            audioInputStream = new FileInputStream(new File(filenameaudio));
        } catch (FileNotFoundException e2) {
            System.err.println("File not found ");
        }

        IContainerFormat videoFormat = IContainerFormat.make();
        IContainerFormat audioFormat = IContainerFormat.make();
        videoFormat.setInputFormat("webm");
        audioFormat.setInputFormat("wav");

        IContainer containerVideo = IContainer.make();
        IContainer containerAudio = IContainer.make();

        if (containerVideo.open(videoInputStream, videoFormat) < 0)
            throw new IllegalArgumentException("Can't find " + filenamevideo);

        if (containerAudio.open(audioInputStream, audioFormat) < 0)
            throw new IllegalArgumentException("Can't find " + filenameaudio);

        // NEW CODE END


        int numStreamVideo = containerVideo.getNumStreams();
        int numStreamAudio = containerAudio.getNumStreams();

        System.out.println("Number of video streams: "+numStreamVideo + "\n" + "Number of audio streams: "+numStreamAudio );

        int videostreamt = -1; //this is the video stream id
        int audiostreamt = -1;

        IStreamCoder  videocoder = null;

        for(int i=0; i<numStreamVideo; i++){
            IStream stream = containerVideo.getStream(i);
            IStreamCoder code = stream.getStreamCoder();

            if(code.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
            {
                videostreamt = i;
                videocoder = code;
                break;
            }

        }

        for(int i=0; i<numStreamAudio; i++){
            IStream stream = containerAudio.getStream(i);
            IStreamCoder code = stream.getStreamCoder();

            if(code.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
            {
                audiostreamt = i;
                break;
            }

        }

        if (videostreamt == -1) throw new RuntimeException("No video stream found");
        if (audiostreamt == -1) throw new RuntimeException("No audio stream found");

        if(videocoder.open()<0 ) throw new RuntimeException("Can't open video coder");
        IPacket packetvideo = IPacket.make();

        IStreamCoder audioCoder = containerAudio.getStream(audiostreamt).getStreamCoder();

        if(audioCoder.open()<0 ) throw new RuntimeException("Can't open audio coder");
        mWriter.addAudioStream(0, 0, audioCoder.getChannels(), audioCoder.getSampleRate());

        mWriter.addVideoStream(1, 1, videocoder.getWidth(), videocoder.getHeight());

        IPacket packetaudio = IPacket.make();

        while(containerVideo.readNextPacket(packetvideo) >= 0 ||
                containerAudio.readNextPacket(packetaudio) >= 0){

            if(packetvideo.getStreamIndex() == videostreamt){

                //video packet
                IVideoPicture picture = IVideoPicture.make(videocoder.getPixelType(),
                        videocoder.getWidth(),
                        videocoder.getHeight());
                int offset = 0;
                while (offset < packetvideo.getSize()){
                    int bytesDecoded = videocoder.decodeVideo(picture,
                            packetvideo,
                            offset);
                    if(bytesDecoded < 0) throw new RuntimeException("bytesDecoded not working");
                    offset += bytesDecoded;

                    if(picture.isComplete()){
                        mWriter.encodeVideo(1, picture);

                    }
                }
            }

            if(packetaudio.getStreamIndex() == audiostreamt){
                //audio packet

                IAudioSamples samples = IAudioSamples.make(512,
                        audioCoder.getChannels(),
                        IAudioSamples.Format.FMT_S32);
                int offset = 0;
                while(offset<packetaudio.getSize())
                {
                    int bytesDecodedaudio = audioCoder.decodeAudio(samples,
                            packetaudio,
                            offset);
                    if (bytesDecodedaudio < 0)
                        throw new RuntimeException("could not detect audio");
                    offset += bytesDecodedaudio;

                    if (samples.isComplete()){
                        mWriter.encodeAudio(0, samples);

                    }
                }

            }

        }
        return outputPath;
    }
}
