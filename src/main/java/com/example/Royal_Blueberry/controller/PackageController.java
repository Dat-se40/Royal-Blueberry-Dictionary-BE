package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.service.PackageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/packages")
public class PackageController
{
    private final PackageService packageService ;
    @PostMapping
    public ResponseEntity<PackageDto> createPackage(@RequestBody PackageDto packageDto)
    {
        PackageDto createdPackage = packageService.createPackage(packageDto);
        return new ResponseEntity<>(createdPackage, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<PackageDto>> getAllPackages()
    {
        List<PackageDto> packages = packageService.getAllPackages();
        return new ResponseEntity<>(packages,HttpStatus.FOUND);
    }
    @GetMapping("{id}")
    public ResponseEntity<PackageDto> getPackage(@PathVariable String id)
    {
        PackageDto foundPackage = packageService.getPackage(id);
        return ResponseEntity.ok(foundPackage);
    }
}
