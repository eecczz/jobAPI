package com.example.jobapi.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QJobPosting is a Querydsl query type for JobPosting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobPosting extends EntityPathBase<JobPosting> {

    private static final long serialVersionUID = -333745104L;

    public static final QJobPosting jobPosting = new QJobPosting("jobPosting");

    public final StringPath closingDate = createString("closingDate");

    public final StringPath company = createString("company");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final StringPath title = createString("title");

    public final StringPath url = createString("url");

    public QJobPosting(String variable) {
        super(JobPosting.class, forVariable(variable));
    }

    public QJobPosting(Path<? extends JobPosting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJobPosting(PathMetadata metadata) {
        super(JobPosting.class, metadata);
    }

}

