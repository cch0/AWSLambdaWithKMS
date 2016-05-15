package cch.aws.kms;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.CreateKeyRequest;
import com.amazonaws.services.kms.model.CreateKeyResult;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;
import com.amazonaws.services.lambda.runtime.Context;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An AWS Lambda to demonstrate round trip encryption without having to specify AWS credentials in the Lambda code.
 */
public class Handler {

    // lambda function arn naming pattern
    private static final Pattern pattern = Pattern
        .compile("\\w+\\:\\w+\\:\\w+\\:(.+)\\:\\d+\\:\\w+\\:\\w+", Pattern.CASE_INSENSITIVE);

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");

    /**
     * Lambda entry point
     * @param request request object which contains plain text to work on
     * @param context context object to determine regions
     * @return result
     */
    public Response handle(Request request, Context context) {
        Regions regions = determineRegions(context.getInvokedFunctionArn());

        AWSKMS awskms = createKMSClient(regions);

        // creating a keyid every time when Lambda is invoked is not required. It is only for demonstration purpose here.
        String keyId = createKeyId(awskms);

        // encrypt the plain text
        ByteBuffer encrypted = encrypt(request.getPlainText(), keyId, awskms);

        // decrypt it
        String decrypted = decrypt(encrypted, awskms);

        Response response = new Response();
        response.setDateTime(simpleDateFormat.format(new Date()));
        response.setDecrypted(decrypted);
        
        return response;
    }

    private String createKeyId(AWSKMS awskms) {
        CreateKeyRequest createKeyRequest = new CreateKeyRequest();
        createKeyRequest.setDescription("demo purpose");
        CreateKeyResult createKeyResult = awskms.createKey(createKeyRequest);
        return createKeyResult.getKeyMetadata().getKeyId();
    }

    private ByteBuffer encrypt(String plainText, String keyId, AWSKMS awskms) {
        EncryptRequest encryptRequest = new EncryptRequest();
        encryptRequest.setPlaintext(ByteBuffer.wrap(plainText.getBytes()));
        encryptRequest.setKeyId(keyId);
        EncryptResult encryptResult = awskms.encrypt(encryptRequest);
        return encryptResult.getCiphertextBlob();
    }

    private String decrypt(ByteBuffer encrypted, AWSKMS awskms) {
        DecryptRequest decryptRequest = new DecryptRequest();
        decryptRequest.setCiphertextBlob(encrypted);
        DecryptResult decryptResult = awskms.decrypt(decryptRequest);
        return new String(decryptResult.getPlaintext().array());
    }

    public static Regions determineRegions(String arn) {
        Matcher matcher = pattern.matcher(arn);

        if (matcher.matches()) {
            return Regions.fromName(matcher.group(1));
        }

        throw new IllegalArgumentException("not able to determine region from " + arn);
    }

    private AWSKMS createKMSClient(Regions regions) {
        AWSKMS awskms = new AWSKMSClient();
        awskms.setEndpoint("https://kms." + regions.getName() + ".amazonaws.com");
        return awskms;
    }
}
