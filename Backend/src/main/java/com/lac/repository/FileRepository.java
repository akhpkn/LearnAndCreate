package com.lac.repository;

import com.lac.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    File findByFileId(Long fileId);

    List<File> findAllByUrlContains(String substring);
}
