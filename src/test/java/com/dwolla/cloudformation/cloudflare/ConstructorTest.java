package com.dwolla.cloudformation.cloudflare;

import com.dwolla.autoscaling.ecs.draining.TerminationEventHandler;

public class ConstructorTest {

    // This needs to compile for the Lambda to be constructable at AWS
    final TerminationEventHandler handler = new TerminationEventHandler();

}
