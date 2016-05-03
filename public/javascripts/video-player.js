var EPSILON = 0.00001;

var videoPlayerId = "video-player";
var playPauseId = "play-pause-button";
var prevId = "prev-section-button";
var nextId = "next-section-button";
var posterId = "poster";
var posterImgId = "poster-img";
var overlayId = "overlay";
var underlayId = "underlay";

var progressClass = "progress";
var progressBarClass = "progress-bar";

var totalDurationAttr = "data-total-duration";
var pathsAttr = "data-paths";
var questionsAttr = "data-questions";
var durationsAttr = "data-durations";

var playGlyphClass = "glyphicon-play";
var pauseGlyphClass = "glyphicon-pause";

var videoPlayer;
var progress;
var videoTime;
var poster;
var overlay;
var overlayDelay;
var underlay;
var paths;
var questions;
var durations;
var totalDuration;
var index;
var numOfVideos;
var volume;
var isPaused;
var noOverlay;
var passedTime;
var noDelay;
var timeoutID;

function playPause() {
    var button = document.getElementById(playPauseId);

    if (noOverlay) {
        if (isPaused) {
            button.getElementsByClassName(pauseGlyphClass)[0].style.display = "inline";
            button.getElementsByClassName(playGlyphClass)[0].style.display = "none";

            videoPlayer.play();
            isPaused = false;
        } else {
            button.getElementsByClassName(playGlyphClass)[0].style.display = "inline";
            button.getElementsByClassName(pauseGlyphClass)[0].style.display = "none";

            videoPlayer.pause();
            isPaused = true;
        }
    } else {
        if (isPaused) {
            button.getElementsByClassName(pauseGlyphClass)[0].style.display = "inline";
            button.getElementsByClassName(playGlyphClass)[0].style.display = "none";

            overlay.style.display = "none";
            underlay.style.display = "inline-block";
            noOverlay = false;

            videoPlayer.play();
            isPaused = false;
        } else {
            button.getElementsByClassName(playGlyphClass)[0].style.display = "inline";
            button.getElementsByClassName(pauseGlyphClass)[0].style.display = "none";

            videoPlayer.pause();
            isPaused = true;
        }
    }
};

function stop() {
    var button = document.getElementById(playPauseId);
    button.getElementsByClassName(playGlyphClass)[0].style.display = "inline";
    button.getElementsByClassName(pauseGlyphClass)[0].style.display = "none";

    isPaused = true;
    index = 0;
    passedTime = 0;
    updateProgressBar(true);

    switchToVideo();
};

function prev() {
    var toLoadIndex = index - 1 > 0 ? index - 1 : 0;
    if (toLoadIndex != index) {
        index = toLoadIndex;

        passedTime -= durations[index];
        if (passedTime < EPSILON) passedTime = 0;
        updateProgressBar(true);

        switchToVideo();
    }
};

function next() {
    var toLoadIndex = index + 1 < numOfVideos - 1 ? index + 1 : numOfVideos - 1;
    if (toLoadIndex != index) {
        passedTime += durations[index];
        updateProgressBar(true);

        index = toLoadIndex;
        switchToVideo();
    } else if (index == numOfVideos - 1) {
        stop();
    }

};

function volumeChanged(direction) {
    if (direction == "up") volume += 0.1;
    else
    if (direction == "down") volume -= 0.1;

    if (0 > volume) volume = 0;
    else
    if (volume > 1) volume = 1;

    videoPlayer.volume = volume;
};

function switchToVideo() {
    videoPlayer.pause();

    underlay.style.display = "none";
    underlay.innerHTML = questions[index];

    overlay.innerHTML = questions[index];
    overlay.style.display = "inline";
    noOverlay = false;

    videoPlayer.src = paths[index];
    videoPlayer.load();

    if (index == 0) {
        document.getElementById(prevId).disabled = true;
    } else {
        document.getElementById(prevId).disabled = false;
    }

    if (index == numOfVideos - 1) {
        document.getElementById(nextId).disabled = true;
    } else {
        document.getElementById(nextId).disabled = false;
    }
};

function takeSnapshot() {
    var canvas = document.createElement("canvas");
    var ctx = canvas.getContext("2d");
    canvas.width = 640;
    canvas.height = 480;
    ctx.drawImage(videoPlayer, 0, 0, canvas.width, canvas.height);
    document.getElementById(posterImgId).src = canvas.toDataURL();
};

function switchToVideoWithTime(toLoadIndex) {
    if (index != toLoadIndex) {
        takeSnapshot();

        underlay.innerHTML = questions[toLoadIndex];

        poster.style.display = "inline";
    }

    if (index != toLoadIndex) {
        index = toLoadIndex;

        videoPlayer.pause();
        videoPlayer.src = paths[index];
        noDelay = true;
        videoPlayer.load();

        if (index == 0) {
            document.getElementById(prevId).disabled = true;
        } else {
            document.getElementById(prevId).disabled = false;
        }

        if (index == numOfVideos - 1) {
            document.getElementById(nextId).disabled = true;
        } else {
            document.getElementById(nextId).disabled = false;
        }
    } else {
        videoPlayer.currentTime = videoTime;
        videoPlayer.pause();
        videoTime = -1.0;

        if (!isPaused) videoPlayer.play();
    }
};

function overlayTimeout() {
    if (isPaused) return;

    overlay.style.display = "none";
    underlay.style.display = "inline-block";
    noOverlay = true;

    videoPlayer.play();
};

function loadComplete() {
    if (noDelay) {
        noDelay = false;
        poster.style.display = "none";
        overlay.style.display = "none";
        underlay.style.display = "inline-block";
        noOverlay = true;

        if (isPaused) return;

        videoPlayer.play();
    } else if (!isPaused) {
        clearTimeout(timeoutID);
        timeoutID = setTimeout(overlayTimeout, overlayDelay);
    }
};

function updateProgressBar(noTimeUpdate) {
    var percentage;
    if (noTimeUpdate == true) percentage = (passedTime) / totalDuration * 100;
    else percentage = (passedTime + videoPlayer.currentTime) / totalDuration * 100;
    percentage = percentage.toString() + "%";

    document.getElementsByClassName(progressBarClass)[0].style.width = percentage;
};

function updateTime() {
    if (videoTime != -1 && videoTime != undefined) {
        videoPlayer.currentTime = videoTime;
        videoTime = -1.0;
    }
};

function skipVideo(e) {
    var offset = $(e.target).offset();
    var time = (e.clientX - offset.left) / progress.offsetWidth * totalDuration;

    passedTime = 0;
    var toLoadIndex = 0;

    while (passedTime + durations[toLoadIndex] <= time) {
        passedTime += durations[toLoadIndex ++];
    }

    videoTime = time - passedTime;
    if (videoTime < EPSILON) videoTime = 0;

    switchToVideoWithTime(toLoadIndex);
};

function initVideoPlayer() {
    videoPlayer = document.getElementById(videoPlayerId);
    videoPlayer.addEventListener("timeupdate", updateProgressBar, false);
    videoPlayer.addEventListener("ended", next, false);
    videoPlayer.addEventListener("loadedmetadata", updateTime, false);
    videoPlayer.addEventListener("loadeddata", loadComplete, false);

    progress = document.getElementsByClassName(progressClass)[0];
    progress.addEventListener("click", skipVideo, false);

    poster = document.getElementById(posterId);
    poster.style.display = "none";

    overlay = document.getElementById(overlayId);
    overlayDelay = 2000;

    underlay = document.getElementById(underlayId);
    underlay.style.display = "none";

    paths = videoPlayer.getAttribute(pathsAttr);
    paths = JSON.parse(paths);

    questions = videoPlayer.getAttribute(questionsAttr);
    questions = JSON.parse(questions);

    durations = videoPlayer.getAttribute(durationsAttr);
    durations = JSON.parse(durations);

    totalDuration = videoPlayer.getAttribute(totalDurationAttr);

    index = 0;

    numOfVideos = paths.length;

    volume = videoPlayer.volume;

    isPaused = true;
    noOverlay = false;
    passedTime = 0;
    noDelay = false;

    switchToVideo();
};

document.addEventListener("DOMContentLoaded", function() { initVideoPlayer(); }, false);
