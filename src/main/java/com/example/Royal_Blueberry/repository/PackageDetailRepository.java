package com.example.Royal_Blueberry.repository;

import com.example.Royal_Blueberry.entity.Package;
import com.example.Royal_Blueberry.entity.PackageDetail;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PackageDetailRepository extends MongoRepository<PackageDetail,String> {
}
