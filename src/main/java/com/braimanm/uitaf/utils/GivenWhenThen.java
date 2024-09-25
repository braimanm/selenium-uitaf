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

package com.braimanm.uitaf.utils;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GivenWhenThen {
    public static void Given(Consumer<String> consumer){
        consumer.accept("Given");
    }
    public static void Then(Consumer<String> consumer){
        consumer.accept("Then");
    }
    public static void When(Consumer<String> consumer){
        consumer.accept("When");
    }
    public static void And(Consumer<String> consumer){
        consumer.accept("And");
    }
}
