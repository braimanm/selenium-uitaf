/*
Copyright 2010-2024 Michael Braiman braimanm@gmail.com
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.braimanm.uitaf.testng;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Parameters;

import java.lang.reflect.Method;

public class TestParameterValidator implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        Parameters paramAnnotation = testMethod.getAnnotation(Parameters.class);
        if (paramAnnotation != null) {
            String[] params = (paramAnnotation).value();
            for (String param : params) {
                String value = testResult.getTestContext().getCurrentXmlTest().getParameter(param);
                if (value == null) {
                    String msg = "Parameter " + param + " for method " + testMethod.getName() +
                            " in class " + testMethod.getDeclaringClass().getName() +
                            " was not found in test suite file!";
                    throw new RuntimeException(msg);
                }
            }
        }
    }

}
