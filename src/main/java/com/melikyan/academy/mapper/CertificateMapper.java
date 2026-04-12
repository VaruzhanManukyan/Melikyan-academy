package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Certificate;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.certificate.CertificateResponse;
import com.melikyan.academy.dto.request.certificate.CreateCertificateRequest;
import com.melikyan.academy.dto.request.certificate.UpdateCertificateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CertificateMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Certificate toEntity(CreateCertificateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateCertificateRequest request, @MappingTarget Certificate certificate);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "purchasableId", source = "purchasable.id")
    CertificateResponse toResponse(Certificate certificate);

    List<CertificateResponse> toResponseList(List<Certificate> certificates);
}
