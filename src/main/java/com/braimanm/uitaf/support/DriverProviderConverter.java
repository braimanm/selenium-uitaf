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

package com.braimanm.uitaf.support;

import ru.qatools.properties.converters.Converter;

public class DriverProviderConverter implements Converter<DriverProvider> {

    @Override
    public DriverProvider convert(String from) throws Exception {
        Class<?> driverProvider;
        try {
            driverProvider = Class.forName(from);
        } catch( ClassNotFoundException e ) {
            throw new RuntimeException("DriverProvider " + from + " is not exist!");
        }
        return (DriverProvider) driverProvider.getDeclaredConstructor().newInstance();
    }
}
