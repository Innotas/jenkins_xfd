package com.innotas.xfd.stoplight.driver;

import com.comsysto.buildlight.cleware.driver.Led;
import com.innotas.xfd.stoplight.ReadableTrafficLight;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of the Cleware TrafficLight that relies on the <code>clewarecontrol</code> command-line tool,
 * rather than on a direct USB connection.  This is useful because the JNI linkage for USB control is difficult (for
 * me) to set up on Linux.
 *
 * <p>
 *     The process issuing the <code>clewarecontrol</code> command seems to require root privileges.
 * </p>
 *
 * @author jstokes
 */
public class ExternalProcessTrafficLight implements ReadableTrafficLight {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProcessTrafficLight.class);

    private static final String EXECUTABLE = "clewarecontrol";

    /** Finds the number in the string "Number of Cleware devices found: 1". */
    private static final Pattern DEVICE_COUNT_REGEX = Pattern.compile("Number of Cleware devices found:\\s*(\\d+)");

    /** Finds the number in the string "serial number: 123456". */
    private static final Pattern DEVICE_SERIAL_NUMBER_REGEX = Pattern.compile("Device.*serial number:\\s*(\\d+)");

    private String deviceSerialNumber;
    private CommandLine switchOffAllLedsCmd;
    private CommandLine switchOnAllLedsCmd;
    private Map<Led,CommandLine> switchOnCmdsByLed;
    private Map<Led,CommandLine> switchOffCmdsByLed;
    private Map<Led,CommandLine> isSwitchOnCmdsByLed;

    public ExternalProcessTrafficLight() throws IOException {
        CommandLine cmd = new CommandLine(EXECUTABLE);
        cmd.addArgument("-l"); // list devices
        String deviceList = executeAndReturnStdOut(cmd);
        int deviceCount = parseDeviceCount(deviceList);
        if (deviceCount == 0) throw new IllegalStateException("Cannot find Cleware devices:\n" + deviceList);
        else if (deviceCount > 1) throw new IllegalStateException("Cannot select a default Cleware device from " + deviceCount + " devices.  Please specify a device by serial number:\n" + deviceList);
        deviceSerialNumber = parseDeviceSerialNumber(deviceList);
        initializeCommands();
    }

    public ExternalProcessTrafficLight(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
        initializeCommands();
    }

    /** Called from the constructor to initialize command objects. */
    private void initializeCommands() {
        switchOffAllLedsCmd = createControlCommand("-as", "0", "0", "-as", "1", "0", "-as", "2", "0");
        switchOnAllLedsCmd = createControlCommand("-as", "0", "1", "-as", "1", "1", "-as", "2", "1");
        switchOffCmdsByLed = new HashMap<>(5);
        switchOffCmdsByLed.put(Led.GREEN, createControlCommand("-as ", String.valueOf(Led.GREEN.ordinal()), "0"));
        switchOffCmdsByLed.put(Led.YELLOW, createControlCommand("-as ", String.valueOf(Led.YELLOW.ordinal()), "0"));
        switchOffCmdsByLed.put(Led.RED, createControlCommand("-as ", String.valueOf(Led.RED.ordinal()), "0"));
        switchOnCmdsByLed = new HashMap<>(5);
        switchOnCmdsByLed.put(Led.GREEN, createControlCommand("-as ", String.valueOf(Led.GREEN.ordinal()), "1"));
        switchOnCmdsByLed.put(Led.YELLOW, createControlCommand("-as ", String.valueOf(Led.YELLOW.ordinal()), "1"));
        switchOnCmdsByLed.put(Led.RED, createControlCommand("-as ", String.valueOf(Led.RED.ordinal()), "1"));
        isSwitchOnCmdsByLed = new HashMap<>(5);
        isSwitchOnCmdsByLed.put(Led.GREEN, createControlCommand("-rs ", String.valueOf(Led.GREEN.ordinal()), "|", "grep", "On"));
        isSwitchOnCmdsByLed.put(Led.YELLOW, createControlCommand("-rs ", String.valueOf(Led.YELLOW.ordinal()), "|", "grep", "On"));
        isSwitchOnCmdsByLed.put(Led.RED, createControlCommand("-rs ", String.valueOf(Led.RED.ordinal()), "|", "grep", "On"));
    }

    private CommandLine createControlCommand(String... additionalArguments) {
        CommandLine cmd = new CommandLine(EXECUTABLE);
        cmd.addArgument("-d ");
        cmd.addArgument(deviceSerialNumber);
        cmd.addArgument("-c");
        cmd.addArgument("1");
        for (String arg : additionalArguments) {
            cmd.addArgument(arg);
        }
        return cmd;
    }

    /**
     * Parses the raw text returned by <code>clewarecontrol -l</code> to find the number of Cleware devices.
     * Warning: this method is called from the ExternalProcessTrafficLight constructor.
     */
    protected int parseDeviceCount(String deviceList) {
        Matcher matcher = DEVICE_COUNT_REGEX.matcher(deviceList);
        if (matcher.find()) {
            String deviceCountString = matcher.group(1);
            return Integer.parseInt(deviceCountString);
        }
        throw new IllegalStateException("No device count found in device list:\n" + deviceList);
    }

    /**
     * Parses the raw text returned by <code>clewarecontrol -l</code> to find the serial/device number of the single
     * Cleware device listed.
     * Warning: this method is called from the ExternalProcessTrafficLight constructor.
     * @throws IllegalStateException if more than one Cleware device is present.
     */
    protected String parseDeviceSerialNumber(String deviceList) {
        Matcher matcher = DEVICE_SERIAL_NUMBER_REGEX.matcher(deviceList);
        if (matcher.find()) {
            String serialNumber = matcher.group(1);
            if (matcher.find()) throw new IllegalStateException("Multiple device serial numbers found in device list:\n" + deviceList);
            return serialNumber;
        }
        else throw new IllegalStateException("No device serial number found in device list:\n" + deviceList);
    }

    /** Called from the constructor. */
    protected String executeAndReturnStdOut(CommandLine cmd) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DefaultExecutor exec = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        exec.setStreamHandler(streamHandler);
        exec.execute(cmd);
        return(outputStream.toString());
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    @Override
    public boolean isLedOn(Led led) {
        int exitCode = execute(isSwitchOnCmdsByLed.get(led));
        return exitCode == 0;
    }

    @Override
    public void switchOn(Led led) {
        execute(switchOnCmdsByLed.get(led));
    }

    @Override
    public void switchOff(Led led) {
        execute(switchOffCmdsByLed.get(led));
    }

    @Override
    public void switchOnAllLeds() {
        execute(switchOnAllLedsCmd);
    }

    @Override
    public void switchOffAllLeds() {
        execute(switchOffAllLedsCmd);
    }

    /**
     * Executes an external command with a standard number of retries.
     * @param cmd the commandline command to issue.
     * @return the exit code from the process.
     */
    protected int execute(CommandLine cmd) {
        return execute(cmd, 2);
    }

    /**
     * Executes an external command.
     * @param cmd the commandline command to issue.
     * @param retries the number of times to retry.  Because the stoplight commands seem to be generally unreliable,
     *                we allow for issuing the same reply a second time if it fails the first time.
     * @return the exit code from the process.
     */
    protected int execute(CommandLine cmd, int retries) {
        Executor exec = new DefaultExecutor();
        // Send stdout and stderr to /dev/null, unless we need it for debugging or error handling.
        ByteArrayOutputStream outAndErrContent = new ByteArrayOutputStream();
        exec.setStreamHandler(new PumpStreamHandler(outAndErrContent));
        try {
            return exec.execute(cmd);
        }
        catch (ExecuteException e) {
            String errorText = outAndErrContent.toString();
            if (retries > 0) {
                LOGGER.warn("Error executing '" + cmd + "'.  Retrying up to 1 more time.\n" + errorText);
                return execute(cmd, retries - 1);
            }
            else {
                LOGGER.error("Error executing '" + cmd + "': " + errorText, e);
                throw new RuntimeException(e.getMessage() + ":\n" + errorText, e);
            }
        }
        catch (IOException e) {
            LOGGER.error("I/O error executing '" + cmd + "': " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        // Nothing to do here.
    }
}
