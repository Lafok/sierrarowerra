package com.sierrarowerra.services.mapper;

import com.sierrarowerra.model.dto.PageDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PageMapper {

    public <T, R> PageDto<R> toDto(Page<T> page, Function<T, R> mapper) {
        return new PageDto<>(
                page.getContent().stream().map(mapper).collect(Collectors.toList()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
