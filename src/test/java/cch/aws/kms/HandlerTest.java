package cch.aws.kms;

import com.amazonaws.regions.Regions;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HandlerTest {
    @Test
    public void determineRegions() {
        // west
        String arn = "arn:aws:lambda:us-west-2:861716072467:function:xxxx";

        Regions regions = Handler.determineRegions(arn);

        assertThat(regions).isEqualTo(Regions.US_WEST_2);

        // east
        arn = "arn:aws:lambda:us-east-1:861716072467:function:xxxx";

        regions = Handler.determineRegions(arn);

        assertThat(regions).isEqualTo(Regions.US_EAST_1);
    }
}
