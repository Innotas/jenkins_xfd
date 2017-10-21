package com.teratory.xfd.stoplight.driver;

import com.comsysto.buildlight.cleware.driver.Led;
import org.apache.commons.exec.CommandLine;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author jstokes
 */
public class ExternalProcessTrafficLightTest {

    private static final String SINGLE_DEVICE_LIST = "Cleware library version: 330\n" +
                                                             "Number of Cleware devices found: 1\n" +
                                                             "Device: 0, type: Switch1 (8), version: 106, serial number: 903556\n";

    private static final String DOUBLE_DEVICE_LIST = "Cleware library version: 330\n" +
                                                             "Number of Cleware devices found: 2\n" +
                                                             "Device: 0, type: Switch1 (8), version: 106, serial number: 903556\n" +
                                                             "Device: 1, type: Switch1 (8), version: 106, serial number: 903557\n";

    private static final String ZERO_DEVICE_LIST = "Cleware library version: 330\n" +
                                                           "Number of Cleware devices found: 0\n";

    private static final String BAD_DEVICE_LIST = "Cleware library version: 330\n" +
                                                          "Some unexpected text that, well, we didn't expect.\n";

    @Test
    public void testParseDeviceCount() throws Exception {
        ExternalProcessTrafficLight testLight = new ExternalProcessTrafficLight("12345");
        assertEquals(1, testLight.parseDeviceCount(SINGLE_DEVICE_LIST));
        assertEquals(2, testLight.parseDeviceCount(DOUBLE_DEVICE_LIST));
        assertEquals(0, testLight.parseDeviceCount(ZERO_DEVICE_LIST));

        try {
            testLight.parseDeviceCount(BAD_DEVICE_LIST);
            fail("Expected exception when parsing bad device list.");
        }
        catch (IllegalStateException ignored) {
            // this is expected.
        }

        testLight.close();
    }

    @Test
    public void testParseDeviceSerialNumber() throws Exception {
        ExternalProcessTrafficLight testLight = new ExternalProcessTrafficLight("12345");
        assertEquals("903556", testLight.parseDeviceSerialNumber(SINGLE_DEVICE_LIST));

        try {
            testLight.parseDeviceSerialNumber(DOUBLE_DEVICE_LIST);
            fail("Expected exception when parsing bad device list.");
        }
        catch (IllegalStateException ignored) {
            // this is expected.
        }

        try {
            testLight.parseDeviceSerialNumber(ZERO_DEVICE_LIST);
            fail("Expected exception when parsing bad device list.");
        }
        catch (IllegalStateException ignored) {
            // this is expected.
        }

        try {
            testLight.parseDeviceSerialNumber(BAD_DEVICE_LIST);
            fail("Expected exception when parsing bad device list.");
        }
        catch (IllegalStateException ignored) {
            // this is expected.
        }

        testLight.close();
    }

    @Test
    public void testExecutedCommands() throws Exception {
        final CommandLine[] lastCommandArray = new CommandLine[1];
        ExternalProcessTrafficLight testLight = new ExternalProcessTrafficLight("12345") {
            @Override
            protected int execute(CommandLine cmd) {
                System.err.println("Mock ExternalProcessTrafficLight would normally execute: " + cmd);
                lastCommandArray[0] = cmd;
                return 0;
            }
        };

        testLight.switchOn(Led.RED);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("0", lastCommandArray[0].getArguments()[5]);
        assertEquals("1", lastCommandArray[0].getArguments()[6]);

        testLight.switchOff(Led.RED);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("0", lastCommandArray[0].getArguments()[5]);
        assertEquals("0", lastCommandArray[0].getArguments()[6]);

        testLight.switchOn(Led.YELLOW);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("1", lastCommandArray[0].getArguments()[5]);
        assertEquals("1", lastCommandArray[0].getArguments()[6]);

        testLight.switchOff(Led.YELLOW);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("1", lastCommandArray[0].getArguments()[5]);
        assertEquals("0", lastCommandArray[0].getArguments()[6]);

        testLight.switchOn(Led.GREEN);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("2", lastCommandArray[0].getArguments()[5]);
        assertEquals("1", lastCommandArray[0].getArguments()[6]);

        testLight.switchOff(Led.GREEN);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("2", lastCommandArray[0].getArguments()[5]);
        assertEquals("0", lastCommandArray[0].getArguments()[6]);

        testLight.switchOnAllLeds();
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("0", lastCommandArray[0].getArguments()[5]);
        assertEquals("1", lastCommandArray[0].getArguments()[6]);
        assertEquals("-as", lastCommandArray[0].getArguments()[7]);
        assertEquals("1", lastCommandArray[0].getArguments()[8]);
        assertEquals("1", lastCommandArray[0].getArguments()[9]);
        assertEquals("-as", lastCommandArray[0].getArguments()[10]);
        assertEquals("2", lastCommandArray[0].getArguments()[11]);
        assertEquals("1", lastCommandArray[0].getArguments()[12]);

        testLight.switchOffAllLeds();
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-as", lastCommandArray[0].getArguments()[4]);
        assertEquals("0", lastCommandArray[0].getArguments()[5]);
        assertEquals("0", lastCommandArray[0].getArguments()[6]);
        assertEquals("-as", lastCommandArray[0].getArguments()[7]);
        assertEquals("1", lastCommandArray[0].getArguments()[8]);
        assertEquals("0", lastCommandArray[0].getArguments()[9]);
        assertEquals("-as", lastCommandArray[0].getArguments()[10]);
        assertEquals("2", lastCommandArray[0].getArguments()[11]);
        assertEquals("0", lastCommandArray[0].getArguments()[12]);

        testLight.isLedOn(Led.RED);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-rs", lastCommandArray[0].getArguments()[4]);
        assertEquals("0", lastCommandArray[0].getArguments()[5]);

        testLight.isLedOn(Led.YELLOW);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-rs", lastCommandArray[0].getArguments()[4]);
        assertEquals("1", lastCommandArray[0].getArguments()[5]);

        testLight.isLedOn(Led.GREEN);
        assertEquals("-d", lastCommandArray[0].getArguments()[0]);
        assertEquals("12345", lastCommandArray[0].getArguments()[1]);
        assertEquals("-rs", lastCommandArray[0].getArguments()[4]);
        assertEquals("2", lastCommandArray[0].getArguments()[5]);

        testLight.close();
    }


    @Test
    public void testNoArgConstructor() throws Exception {
        final CommandLine[] lastCommandArray = new CommandLine[1];
        final String[] deviceListStringToReturn = new String[] { SINGLE_DEVICE_LIST};
        class NoArgConstructorTestExternalProcessTrafficLight extends ExternalProcessTrafficLight {

            public NoArgConstructorTestExternalProcessTrafficLight() throws IOException {
                super();
            }

            @Override
            protected String executeAndReturnStdOut(CommandLine cmd) {
                System.err.println("Mock ExternalProcessTrafficLight would normally execute: " + cmd);
                lastCommandArray[0] = cmd;
                return deviceListStringToReturn[0];
            }
        };
        ExternalProcessTrafficLight testLight = new NoArgConstructorTestExternalProcessTrafficLight();
        assertEquals("-l", lastCommandArray[0].getArguments()[0]);
        assertEquals("903556", testLight.getDeviceSerialNumber());
        testLight.close();

        deviceListStringToReturn[0] = DOUBLE_DEVICE_LIST;
        try {
            new NoArgConstructorTestExternalProcessTrafficLight();
            fail("Exception constructor exception when bad device list is returned.");
        }
        catch (IllegalStateException ignored) {
        }
        assertEquals("-l", lastCommandArray[0].getArguments()[0]);

        deviceListStringToReturn[0] = ZERO_DEVICE_LIST;
        try {
            new NoArgConstructorTestExternalProcessTrafficLight();
            fail("Exception constructor exception when bad device list is returned.");
        }
        catch (IllegalStateException ignored) {
        }
        assertEquals("-l", lastCommandArray[0].getArguments()[0]);
    }
}