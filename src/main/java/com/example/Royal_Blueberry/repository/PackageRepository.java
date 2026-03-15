package com.example.Royal_Blueberry.repository;

import com.example.Royal_Blueberry.entity.Package;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRepository extends MongoRepository<Package,String> {
}
