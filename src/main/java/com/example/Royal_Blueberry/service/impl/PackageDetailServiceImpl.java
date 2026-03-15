package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.PackageDetail;
import com.example.Royal_Blueberry.mapper.PackageDetailMapper;
import com.example.Royal_Blueberry.repository.PackageDetailRepository;
import com.example.Royal_Blueberry.repository.PackageRepository;
import com.example.Royal_Blueberry.service.PackageDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageDetailServiceImpl implements PackageDetailService {
    private final PackageRepository repository;
    private final PackageDetailRepository packageDetailRepository;
    @Override
    public PackageDetailDto createPackageDetail(String packageID, PackageDetailDto packageDetailDto) {
        var targetPackage = repository.findById(packageID).orElseThrow(() ->
                new RuntimeException("Package ID " + packageID + " is not found"));

        packageDetailDto.setPackageId(packageID);
        var savedPackageDetail = packageDetailRepository.save(
                PackageDetailMapper.toEntity(packageDetailDto)
        );

        // Sync totalWords về Package
        targetPackage.setTotalWords(savedPackageDetail.getWords().size());
        targetPackage.setUpdateAt(LocalDateTime.now());
        repository.save(targetPackage);

        return PackageDetailMapper.toDto(savedPackageDetail);
    }
    @Override
    public PackageDetailDto addWord(String packageId, WordEntryDto wordDetailDto) {
        var targetDetailDto = getDetailByPackageId(packageId);
        targetDetailDto.getWords().add(wordDetailDto);
        var savedDetail = packageDetailRepository.save(PackageDetailMapper.toEntity(targetDetailDto));
        return PackageDetailMapper.toDto(savedDetail);
    }

    @Override
    public PackageDetailDto deleteWord(String packageId, String wordName) {
        return null;
    }

    @Override
    public PackageDetailDto getDetailByPackageId(String packageId) {
        List<PackageDetail> packageDetailList = packageDetailRepository.findAll();
        var result = packageDetailList.stream().filter(packageDetail -> packageDetail.getPackageId().equals(packageId))
                .findFirst().orElseThrow(() -> new RuntimeException("Package ID is " + packageId + "is not found"));
        return PackageDetailMapper.toDto(result);
    }

    @Override
    public List<PackageDetailDto> getAllDetails() {
        List<PackageDetail> packageDetailList = packageDetailRepository.findAll();
        List<PackageDetailDto> dtos = new ArrayList<>();
        packageDetailList.forEach(packageDetail -> dtos.add(PackageDetailMapper.toDto(packageDetail)));
        return dtos;
    }

}

