package com.careerthon.service;

import com.careerthon.model.Course;
import com.careerthon.model.Lecture;
import com.careerthon.repository.CourseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@SuppressWarnings("null")
public class YoutubeImportService {

    private final CourseRepository courseRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public YoutubeImportService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Course importPlaylist(String playlistUrl) {
        try {
            // Fetch YouTube HTML
            String html = restTemplate.getForObject(playlistUrl, String.class);
            if (html == null) {
                throw new RuntimeException("Failed to fetch YouTube page");
            }

            // Extract ytInitialData JSON string
            Pattern pattern = Pattern.compile("var ytInitialData = (\\{.*?\\});</script>");
            Matcher matcher = pattern.matcher(html);
            if (!matcher.find()) {
                throw new RuntimeException("Could not find ytInitialData in HTML");
            }

            String jsonString = matcher.group(1);
            JsonNode root = objectMapper.readTree(jsonString);

            // Extract Playlist Title
            String playlistTitle = extractPlaylistTitle(root);
            if (playlistTitle == null || playlistTitle.isEmpty()) {
                playlistTitle = "Imported YouTube Course";
            }

            // Create Course
            Course course = new Course(
                    playlistTitle,
                    "This course was imported from YouTube playlist: " + playlistUrl,
                    "YouTube Creator",
                    null,
                    "Beginner to Advanced",
                    "Imported",
                    "Self-paced",
                    0
            );

            // Extract Videos
            List<VideoInfo> videos = extractVideos(root);
            
            // Set thumbnail to first video's thumbnail
            if (!videos.isEmpty()) {
                course.setThumbnailUrl(videos.get(0).thumbnailUrl);
            }

            int moduleOrder = 1;
            int lectureOrder = 1;
            int currentModuleVideos = 0;
            String currentModuleName = "Module " + moduleOrder;

            for (VideoInfo video : videos) {
                // Group every 10 videos into a new module
                if (currentModuleVideos >= 10) {
                    moduleOrder++;
                    lectureOrder = 1;
                    currentModuleVideos = 0;
                    currentModuleName = "Module " + moduleOrder;
                }

                Lecture lecture = new Lecture(
                        video.title,
                        "",
                        video.videoId,
                        currentModuleName,
                        moduleOrder,
                        lectureOrder,
                        video.duration
                );
                course.addLecture(lecture);
                
                lectureOrder++;
                currentModuleVideos++;
            }

            course.setTotalLectures(videos.size());
            return courseRepository.save(course);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error importing YouTube playlist: " + e.getMessage());
        }
    }

    private String extractPlaylistTitle(JsonNode root) {
        try {
            return root.path("header").path("playlistHeaderRenderer")
                       .path("title").path("simpleText").asText();
        } catch (Exception e) {
            try {
                // Alternative path
                return root.path("metadata").path("playlistMetadataRenderer")
                           .path("title").asText();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private List<VideoInfo> extractVideos(JsonNode root) {
        List<VideoInfo> videos = new ArrayList<>();
        try {
            JsonNode tabs = root.path("contents").path("twoColumnBrowseResultsRenderer")
                               .path("tabs");
            JsonNode playlistVideoList = null;
            
            if (tabs.isArray() && tabs.size() > 0) {
                JsonNode content = tabs.get(0).path("tabRenderer").path("content");
                playlistVideoList = content.path("sectionListRenderer").path("contents").get(0)
                        .path("itemSectionRenderer").path("contents").get(0)
                        .path("playlistVideoListRenderer").path("contents");
            }
            
            if (playlistVideoList == null || !playlistVideoList.isArray()) {
                return videos;
            }

            for (JsonNode item : playlistVideoList) {
                JsonNode videoRenderer = item.path("playlistVideoRenderer");
                if (videoRenderer.isMissingNode()) continue;

                String videoId = videoRenderer.path("videoId").asText();
                String title = "Unknown Title";
                JsonNode titleNode = videoRenderer.path("title").path("runs");
                if (titleNode.isArray() && titleNode.size() > 0) {
                    title = titleNode.get(0).path("text").asText();
                }

                String duration = videoRenderer.path("lengthText").path("simpleText").asText();
                if (duration.isEmpty()) duration = "Unknown";

                String thumbnailUrl = "";
                JsonNode thumbnails = videoRenderer.path("thumbnail").path("thumbnails");
                if (thumbnails.isArray() && thumbnails.size() > 0) {
                    // Get the last thumbnail (usually highest res)
                    thumbnailUrl = thumbnails.get(thumbnails.size() - 1).path("url").asText();
                }

                if (!videoId.isEmpty() && !title.isEmpty()) {
                    videos.add(new VideoInfo(videoId, title, duration, thumbnailUrl));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videos;
    }

    private static class VideoInfo {
        String videoId;
        String title;
        String duration;
        String thumbnailUrl;

        VideoInfo(String videoId, String title, String duration, String thumbnailUrl) {
            this.videoId = videoId;
            this.title = title;
            this.duration = duration;
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}
