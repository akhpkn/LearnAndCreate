package com.lac.model;

import com.lac.payload.CommentInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@Table(name = "comments")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @NotBlank
    @Size(max = 1000)
    private String text;

    @Min(1)
    @Max(5)
    private Integer mark;

    @Column(name = "date_of_creation")
    private String date;

    @OneToOne
    @JoinTable(name = "comment_user",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private User user;

    public Comment(String text, Integer mark){
        String months[] = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
                "октября", "ноября", "декабря"};
        GregorianCalendar calendar = new GregorianCalendar();
        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
        Integer month = calendar.get(Calendar.MONTH);
        Integer year = calendar.get(Calendar.YEAR);
        date = String.format(day + " " + months[month] + " " + year);
        this.text = text;
        this.mark = mark;
    }

    public CommentInfo commentInfo() {
        return CommentInfo.builder()
                .commentId(commentId)
                .date(date)
                .mark(mark)
                .text(text)
                .userId(user.getUserId())
                .userImageUrl(user.getImage() == null ? "url" : user.getImage().getUrl())
                .userName(user.getName())
                .userUsername(user.getUsername())
                .build();
    }
}
