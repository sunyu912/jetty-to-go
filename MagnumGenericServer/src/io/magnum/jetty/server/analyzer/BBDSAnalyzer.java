package io.magnum.jetty.server.analyzer;

import io.magnum.jetty.server.url.ResourceLocator;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import com.hd4ar.awscommon.AwsS3Helper;
import com.hd4ar.awscommon.retry.AbortException;

abstract public class BBDSAnalyzer {
    
    @Autowired
    private AwsS3Helper s3Helper;
    
    public File syncInputFile(String id) throws AbortException {
        File f = ResourceLocator.getLocalImageFile(id);
        if (!f.exists()) {
            s3Helper.syncS3FilesToLocal(ResourceLocator.IMAGE_BUCKET, f.getName(), f.getParent());
            f = ResourceLocator.getLocalImageFile(id);
        }
        return f;
    }
        
}
