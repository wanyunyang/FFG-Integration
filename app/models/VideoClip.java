package models;

import javax.persistence.*;
import play.db.ebean.*;

/**
 * Represents a video clip. Each VideoClip has paths to the video and audio files, and is connected to a Video.
 * Each video is associated with the question that it was answering.
 */
@Entity
public class VideoClip extends Model {
    /**
     * The database ID of the Video
     */
    @Id
    private Long id;
    /**
     * The path to the video file of the video clip, without sound
     */
    private String videoClipPath;
    /**
     * The path to the audio file of the video clip
     */
    private String audioClipPath;
    /**
     * The duration of the clip
     */
    private double duration;
    /**
     * The Video that the VideoClip is associated to
     */
    @ManyToOne
    private Video video;
    /**
     * The question that the Video is answering
     */
    @ManyToOne
    private Question question;

    /**
     * Path to the combined audio and video clip
     */
    private String outputPath;
    /**
     * ID of the uploaded youtube clip (if any)
     */
    private String youtubeID;

    /**
     * Finder used to search the database for VideoClip objects.
     */
    public static Finder<Long,VideoClip> find = new Finder<>(Long.class, VideoClip.class);

    /**
     * The VideoClip constructor. Creates a VideoClip object.
     * @param videoClipPath The path to the video file of the clip.
     * @param audioClipPath The path to the audio file of the clip.
     * @param question The question associated with the clip.
     * @param duration The duration of the clip.
     */
    public VideoClip(String videoClipPath, String audioClipPath, Question question, double duration) {
        this.videoClipPath = videoClipPath;
        this.audioClipPath = audioClipPath;
        this.question = question;
        this.duration = duration;
    }

    /**
     * Setter for video
     * @param video The Video to associate the VideoClip with.
     */
    public void setVideo(Video video) {
        this.video = video;
    }

    /**
     * Getter for video.
     * @return The Video that the VideoClip is associated with.
     */
    public Video getVideo() { return video; }

    /**
     * Getter for ID.
     * @return The database ID of this Video.
     */
    public Long getId() { return id; }

    /**
     * Getter for videoClipPath.
     * @return The path to the video clip of this VideoClip.
     */
    public String getVideoPath() { return videoClipPath; }

    /**
     * Getter for audioClipPath.
     * @return The path to the audio clip of this audioClipPath.
     */
    public String getAudioPath() { return audioClipPath; }

    /**
     * Getter for duration.
     * @return The duration of this VideoClip.
     */
    public double getDuration() { return duration; }

    /**
     * Getter for question.
     * @return The Question associated with this VideoClip.
     */
    public Question getQuestion() { return question; }

    public String getYoutubeID() {
        return youtubeID;
    }

    public void setYoutubeID(String youtubeID) {
        this.youtubeID = youtubeID;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
