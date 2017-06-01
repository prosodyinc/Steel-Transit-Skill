/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amazon.util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 *
 * @author Adithya
 */
public class ImageUploader {

    private static final String SUFFIX = "/";

    public static void uploadImage(String imageURL, String imageName, String folderName, String bucketName) throws MalformedURLException, IOException {
        // credentials object identifying user for authentication

        AWSCredentials credentials = new BasicAWSCredentials(
            System.getenv("AWS_S3_ACCESS_KEY"), 
            System.getenv("AWS_S3_SECRET_ACCESS_KEY"));

        // create a client connection based on credentials
        AmazonS3 s3client = new AmazonS3Client(credentials);


        try {
            if (!(s3client.doesBucketExist(bucketName))) {
                s3client.setRegion(Region.getRegion(Regions.US_EAST_1));
                // Note that CreateBucketRequest does not specify region. So bucket is 
                // created in the region specified in the client.
                s3client.createBucket(new CreateBucketRequest(
                        bucketName));
            }

            //Enabe CORS:
            //     <?xml version="1.0" encoding="UTF-8"?>
            //<CORSConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
            //    <CORSRule>
            //        <AllowedOrigin>http://ask-ifr-download.s3.amazonaws.com</AllowedOrigin>
            //        <AllowedMethod>GET</AllowedMethod>
            //    </CORSRule>
            //</CORSConfiguration>
            BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration();

            CORSRule corsRule = new CORSRule()
                    .withAllowedMethods(Arrays.asList(new CORSRule.AllowedMethods[]{
                        CORSRule.AllowedMethods.GET}))
                    .withAllowedOrigins(Arrays.asList(new String[]{"http://ask-ifr-download.s3.amazonaws.com"}));
            configuration.setRules(Arrays.asList(new CORSRule[]{corsRule}));
            s3client.setBucketCrossOriginConfiguration(bucketName, configuration);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which "
                    + "means your request made it "
                    + "to Amazon S3, but was rejected with an error response"
                    + " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which "
                    + "means the client encountered "
                    + "an internal error while trying to "
                    + "communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        String fileName = folderName + SUFFIX + imageName + ".png";
        URL url = new URL(imageURL);

        ObjectMetadata omd = new ObjectMetadata();
        omd.setContentType("image/png");
        omd.setContentLength(url.openConnection().getContentLength());
        // upload file to folder and set it to public
        s3client.putObject(new PutObjectRequest(bucketName, fileName, url.openStream(), omd)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    //public static void main(String[] args) throws MalformedURLException, IOException {
      //  uploadImage("https://maps.googleapis.com/maps/api/staticmap?size=1200x800&maptype=roadmap&key=AIzaSyAOTkkr2SDnAQi8-fohOn4rUinICd-pHVA&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:1%7C40.43325099999999,-79.923598&markers=size:mid%7Ccolor:0xff0000%7Clabel:2%7C40.433700261186,-79.922931511244&path=color:0x0000ff|weight:5|40.43338,-79.92379|40.43338,-79.92379|40.43342,-79.92375|40.43342,-79.92375|40.43346,-79.9237|40.43346,-79.9237|40.4335,-79.92364|40.4335,-79.92364|40.43354,-79.92358|40.43354,-79.92358|40.43358,-79.92351|40.43358,-79.92351|40.43361,-79.92344|40.43361,-79.92344|40.43363,-79.92338|40.43363,-79.92338|40.43366,-79.92331|40.43366,-79.92331|40.43368,-79.92324|40.43368,-79.92324|40.4337,-79.92317|40.4337,-79.92317|40.43373,-79.92296|40.43373,-79.92296", "abc", "image", "ppas-image-upload");
    //}
}
