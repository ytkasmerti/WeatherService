package ru.urfu.webapplication.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "popular_searches")
@Data

public class PopularSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(name = "request_type", nullable = false)
    private String requestType;

    @Column(name = "search_count", nullable = false)
    private Integer searchCount = 0; // счетчик количества поисков

    @Column(name = "last_searched")
    private LocalDateTime lastSearched; // когда в последний раз был такой запрос
}
