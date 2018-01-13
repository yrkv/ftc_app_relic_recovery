package org.firstinspires.ftc.teamcode.mecanum;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Button;
import org.firstinspires.ftc.teamcode.ButtonEvent;
import org.firstinspires.ftc.teamcode.Motor8696;
import org.firstinspires.ftc.teamcode.MotorState;
import org.firstinspires.ftc.teamcode.ServoTarget;

/**
 * Created by USER on 1/7/2018.
 */

@TeleOp(name="FancyDriving", group="Temp")
public class FancyDriving extends MecanumOpMode {
    /**
     * How many encoder counts the lifts need to
     * rotate to elevate the lift by one block.
     */
    private final static int LIFT_BLOCK_HEIGHT = 3000;

    private int liftPosition = 0;

    private int reverseLeft = 1;
    private int reverseRight = 1;

    @Override
    void buttonEvents() {
        super.buttonEvents();

        addButtonEvent(2, new ButtonEvent(Button.UP) {
            public void onDown() {
                liftPosition++;
                if (liftPosition > 3)
                    liftPosition = 3;
                setLiftTarget();
            }
        });

        addButtonEvent(2, new ButtonEvent(Button.DOWN) {
            public void onDown() {
                liftPosition--;
                if (liftPosition < -1)
                    liftPosition = -1;
                setLiftTarget();
            }
        });
    }

    private void setLiftTarget() {
        leftLift.setTargetPosition(liftPosition * LIFT_BLOCK_HEIGHT);
        rightLift.setTargetPosition(liftPosition * LIFT_BLOCK_HEIGHT);

        leftLift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightLift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    private void runLift() {
        int leftDiff = Math.abs(liftPosition * LIFT_BLOCK_HEIGHT - leftLift.getCurrentPosition());
        int rightDiff = Math.abs(liftPosition * LIFT_BLOCK_HEIGHT - rightLift.getCurrentPosition());

        if (leftDiff > rightDiff) {
            leftLift.setPower(0.51);
            rightLift.setPower(0.5);
        } else if (leftDiff > rightDiff) {
            leftLift.setPower(0.5);
            rightLift.setPower(0.51);
        } else {
            leftLift.setPower(0.5);
            rightLift.setPower(0.5);
        }
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initRobot();
        buttonEvents();

        setLiftTarget();

        waitForStart();


        double power = 0.35;

        while (opModeIsActive()) {

            getGyroData();

            if (collectorActive) {
                if (gamepad2.right_bumper) {
                    leftCollector.setPower(-power);
                    rightCollector.setPower(-power);
                } else if (gamepad2.b) {
                    leftCollector.setPower(power);
                    rightCollector.setPower(-power);
                } else if (gamepad2.x) {
                    leftCollector.setPower(-power);
                    rightCollector.setPower(power);
                } else {
                    leftCollector.setPower(power * reverseLeft);
                    rightCollector.setPower(power * reverseRight);
                }
            } else {
                leftCollector.setPower(0);
                rightCollector.setPower(0);
            }

//            leftLift.setPower(gamepad2.left_stick_y);
//            rightLift.setPower(gamepad2.left_stick_y);

            runLift();

            telemetry.addData("angles", angles.toString());

            ballPoosher.setPosition(gamepad2.left_trigger);

            telemetry.addData(leftCollector.getDeviceName(), robotState.motors[4].speed);
            telemetry.addData(rightCollector.getDeviceName(), robotState.motors[5].speed);

            telemetry.addData("gamepadLeft", getGamepadAngle(gamepad1.left_stick_x, gamepad1.left_stick_y));
            telemetry.addData("gamepadRight", getGamepadAngle(gamepad1.right_stick_x, gamepad1.right_stick_y));

            if (!dpadDrive()) {

                mecanumTeleOpDrive();
//                if (reverse > 0)
//                    verticalDrive(reverse * driveSpeed, gamepad1.left_stick_y, gamepad1.right_stick_y);
//                else
//                    verticalDrive(reverse * driveSpeed, gamepad1.right_stick_y, gamepad1.left_stick_y);
//                horizontalDrive(reverse * driveSpeed, (gamepad1.right_trigger - gamepad1.left_trigger));
//
//                for (Motor8696 motor : motors) {
//                    motor.setMaxPower(Math.sqrt(2));
//                }

//                runMotors();
            }

            if (!cubePoosherTarget.runServo()) cubePoosherTarget = ServoTarget.NULL;

            runButtonEvents();
            periodic(100);

            telemetry.update();
        }
    }

    private void checkCollectors() {
        reverseLeft = reverseRight = 1;
        if (checkCollector(robotState.motors[4])) {
            reverseLeft = -1;
        }
        else if (checkCollector(robotState.motors[5])) {
            reverseRight = -1;
        }
    }

    private boolean checkCollector(MotorState motorState) {
        if (motorState.power == 0 || motorState.lastPower == 0)
            return false;
        if (motorState.speed < 1) {// TODO: change the shit out of this
            return true;
        }

        return false;
    }
}
