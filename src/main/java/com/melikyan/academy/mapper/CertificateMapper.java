package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Certificate;
import com.melikyan.academy.dto.response.certificate.CertificateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CertificateMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "contentItemId", source = "contentItem.id")
    CertificateResponse toResponse(Certificate certificate);

    List<CertificateResponse> toResponseList(List<Certificate> certificates);
}
