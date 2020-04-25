package com.lac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.*;

@Table(name = "courses")
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "description_long")
    private String descriptionLong;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "course_image",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    private Image image;

    @ManyToOne
    @JoinTable(name = "course_category",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Category category;

//    @NotBlank
//    @Column(name = "date_creation")
//    private Date date;
//
//    @NotBlank
//    private Integer duration;
//
    private Double mark = 0.0;

    @Column(name = "num_marks")
    private Long numMarks = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(name = "user_courses",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

//    @OneToOne(fetch = FetchType.LAZY)
//    @JsonIgnore
//    @JoinTable(name = "creator_user",
//            joinColumns = @JoinColumn(name = "course_id"),
//            inverseJoinColumns = @JoinColumn(name = "user_id"))
//    private User creator = new User();

//    @OneToOne(fetch = FetchType.LAZY)
//    @JsonIgnore
//    @JoinTable(name = "course_video",
//            joinColumns = @JoinColumn(name = "course_id"),
//            inverseJoinColumns = @JoinColumn(name = "file_id"))
//    private Video video = new Video();


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinTable(name = "course_lessons",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id"))
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(name = "course_comments",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "comment_id"))
    private Set<Comment> comments = new HashSet<>();

//    @ManyToMany( fetch =  FetchType.LAZY)
//    @JsonIgnore
//    @JoinTable(name = "course_tags",
//            joinColumns = @JoinColumn(name  = "course_id"),
//            inverseJoinColumns =  @JoinColumn(name = "tag_id"))
//    private Set<Tag> tags = new HashSet<>();// потом заменим на нормальный тег

    public Course() {

    }

    public Course(String title, String description, String descriptionLong, Category category) {
        this.title = title;
        this.description = description;
        this.descriptionLong = descriptionLong;
        this.category = category;
    }

    public String getDescriptionLong() {
        return descriptionLong;
    }

    public void setDescriptionLong(String descriptionLong) {
        this.descriptionLong = descriptionLong;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Category getCategory() {
        return category;
    }

    public Image getImage() {
        return image;
    }

//    public Video getVideos() {
//        return video;
//    }

//    public void setVideos(Video video) {
//        this.video = video;
//    }


    public void setCategory(Category category) {
        this.category = category;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public Date getDate() {
//        return date;
//    }
//
//    public void setDate(Date date) {
//        this.date = date;
//    }
//
//    public Integer getDuration() {
//        return duration;
//    }
//
//    public void setDuration(Integer duration) {
//        this.duration = duration;
//    }
//
    public Double getMark() {
        return mark;
    }

    public void setMark(Double mark) {
        this.mark = mark;
    }

    public Long getNumMarks() {
        return numMarks;
    }

    public void setNumMarks(Long numMarks) {
        this.numMarks = numMarks;
    }
//
//    public User getCreator() {
//        return creator;
//    }
//
//    public void setCreator(User creator) {
//        this.creator = creator;
//    }

//    public Video getVideo() {
//        return video;
//    }

//    public void setVideo(Video video) {
//        this.video = video;
//    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

    public void deleteComment(Comment comment){
        comments.remove(comment);
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }
}