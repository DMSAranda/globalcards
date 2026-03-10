import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

public class S3Test {

    public static void main(String[] args) {

        S3Client s3 = S3Client.builder().build();

        ListBucketsResponse buckets = s3.listBuckets();

        buckets.buckets().forEach(b ->
                System.out.println(b.name())
        );
    }
}