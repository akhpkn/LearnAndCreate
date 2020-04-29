package com.lac.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Calendar;
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
    @Size(max = 10000)
    private String text;

    @Min(1)
    @Max(5)
    private Integer mark;

    @Basic
    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;

    @OneToOne
    @JoinTable(name = "comment_user",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private User user;

    public Comment(String text, Integer mark){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        date = calendar;
        this.text = text;
        this.mark = mark;
    }
}
