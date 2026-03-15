package com.example.Royal_Blueberry.service;

import com.example.Royal_Blueberry.dto.PackageDto;

import java.util.List;

public interface PackageService {
    PackageDto createPackage(PackageDto packageDto);
    PackageDto getPackage(String id);

    List<PackageDto> getAllPackages() ;
}
