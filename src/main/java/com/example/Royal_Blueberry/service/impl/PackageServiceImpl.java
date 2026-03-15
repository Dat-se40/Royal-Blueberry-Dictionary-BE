package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.entity.Package;
import com.example.Royal_Blueberry.mapper.PackageMapper;
import com.example.Royal_Blueberry.repository.PackageRepository;
import com.example.Royal_Blueberry.service.FindWordService;
import com.example.Royal_Blueberry.service.PackageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final PackageRepository repository;
    @Override
    public PackageDto createPackage(PackageDto packageDto) {
        Package _package = PackageMapper.mapToPackage(packageDto);
        _package.setUpdateAt(LocalDateTime.now());
        return PackageMapper.mapToPackageDto(repository.save(_package));
    }

    @Override
    public PackageDto getPackage(String id) {
        return PackageMapper.mapToPackageDto(repository.findById(id).get());
    }

    @Override
    public List<PackageDto> getAllPackages() {
        var result = repository.findAll();
        List<PackageDto> packageDtos = new ArrayList<PackageDto>();
        result.forEach( r -> packageDtos.add(PackageMapper.mapToPackageDto(r)));
        return packageDtos;
    }
}
