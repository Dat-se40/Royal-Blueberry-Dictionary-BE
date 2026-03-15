package com.example.Royal_Blueberry.service;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.PackageDetail;

import java.util.List;

public interface PackageDetailService {
    PackageDetailDto createPackageDetail(String packageID,PackageDetailDto packageDetailDto);
    PackageDetailDto addWord(String packageId, WordEntryDto wordDetailDto);
    PackageDetailDto deleteWord(String packageId, String wordName);
    PackageDetailDto getDetailByPackageId(String packageId);
    List<PackageDetailDto> getAllDetails() ;
}
