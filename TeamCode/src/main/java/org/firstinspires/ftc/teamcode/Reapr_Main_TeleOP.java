/* Base code from:
https://gm0.org/en/latest/docs/software/tutorials/mecanum-drive.html

Main TeleOP (1 Driver)
*/

package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "TeleOP")

public class Reapr_Main_TeleOP extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        // Declare our motors
        // Make sure your ID's match your configuration

        // Meccanum Drivetrain
        DcMotor motorFrontLeft = hardwareMap.dcMotor.get("motorFrontLeft"); // Port 0
        DcMotor motorBackLeft = hardwareMap.dcMotor.get("motorBackLeft"); // Port 1
        DcMotor motorFrontRight = hardwareMap.dcMotor.get("motorFrontRight"); // Port 2
        DcMotor motorBackRight = hardwareMap.dcMotor.get("motorBackRight"); // Port 3

        // Reverse the right side motors
        // Reverse left motors if you are using NeveRests
        motorFrontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        motorBackLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        // motorBackRight.setDirection(DcMotorSimple.Direction.REVERSE);  // This was connected on the expansion hub, it needs to be reversed


        DcMotor spinnerMotor = hardwareMap.dcMotor.get("spinnerMotor"); // Port 0

        // Servo (airplane launcher)
        // On port 0
        Servo launcher = hardwareMap.servo.get("launcher");// name of server on control hub is reaprClaw
        double launcherPosition = 0;
        final double launcherSpeed = 0.1;// change to 100th when button is hold
        final double launcherMinRange = 0.3;
        final double launcherMaxRange = 0.55;

        // Servo 2(arm)
        // On port 0
        Servo arm = hardwareMap.servo.get("arm");// name of server on control hub is reaprClaw
        double armPosition = 0;
        final double armSpeed = 0.1;// change to 100th when button is hold
        final double armMinRange = 0.3;
        final double armMaxRange = 2.95;




        waitForStart();

        if (isStopRequested()) return;

        boolean isSlowMode = false;
        double dividePower=1.0;

        while (opModeIsActive()) {
            // Control Speed
            if(isSlowMode){
                dividePower=1.5;
            }else{
                dividePower=1.0;
            }

            if(gamepad1.left_stick_button){
                if(isSlowMode){
                    isSlowMode=false;
                    sleep(500);
                }else{
                    isSlowMode=true;
                    sleep(500);
                }
            }

            // Mecccanum controls
            double y = -gamepad1.left_stick_y; // Remember, this is reversed!
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;


            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio, but only when
            // at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator / dividePower; //Positive rotation results in forward & right motion
            double backLeftPower = (y - x + rx) / denominator / dividePower; //Positive rotation results in forward & left motion
            double frontRightPower = (y - x - rx) / denominator / dividePower; //Positive rotation results in forward & left motion
            double backRightPower = (y + x - rx) / denominator / dividePower; //Positive rotation results in forward & right motion

            motorFrontLeft.setPower(frontLeftPower);
            motorBackLeft.setPower(backLeftPower);
            motorFrontRight.setPower(frontRightPower);
            motorBackRight.setPower(backRightPower);



            // Spinner motor Controls

/*
            if (gamepad1.a){ // Move down
                spinnerMotor.setPower(1);
            }
            spinnerMotor.setPower(0);

            if (gamepad1.y){ // Move up
                spinnerMotor.setPower(-1);
            }
            spinnerMotor.setPower(0);

*/

            // Servo Controls

            if (gamepad1.b) { // Down
                launcherPosition += launcherSpeed;
                launcherPosition = Range.clip(launcherPosition, launcherMinRange, launcherMaxRange);
                launcher.setPosition(launcherPosition);

                telemetry.addData("launcher", "%.2f", launcherPosition); //displays the values on the driver hub
                telemetry.update();

            }
            else if (gamepad1.x) { // Up
                launcherPosition -= launcherSpeed;
            }


            // Arm controls
            if (gamepad1.a) { // Down
                armPosition += armSpeed;
                //armPosition = Range.clip(armPosition, armMinRange, armMaxRange);
                arm.setPosition(armPosition);

                telemetry.addData("arm", "%.2f", launcherPosition); //displays the values on the driver hub
                telemetry.update();

            }
            else if (gamepad1.y) { // Up
                armPosition -= armSpeed;
                //armPosition = Range.clip(armPosition, armMinRange, armMaxRange);
                arm.setPosition(armPosition);

                telemetry.addData("arm", "%.2f", launcherPosition); //displays the values on the driver hub
                telemetry.update();
            }
        }
    }
}
