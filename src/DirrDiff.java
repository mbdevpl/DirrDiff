import pl.mbdev.dirrdiff.MainFrame;

/**
 * 
 */

/**
 * Main class of DirrDiff application. DirrDiff stands for Directory Recursive
 * Differentiation. This class only launches the main frame.<br />
 * <br />
 * File creation date: Jul 28, 2012, 5:20:33 PM. This is a part of DirrDiff.
 * 
 * <pre>
 * Copyright 2012 Mateusz Bysiek,
 *     mb@mbdev.pl, http://mbdev.pl/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </pre>
 * 
 * @author &copy; 2012 Mateusz Bysiek <a href="http://mbdev.pl/">http://mbdev.pl/</a>
 */
public final class DirrDiff {
	
	/**
	 * Launches DirrDiff.
	 * 
	 * @param args
	 *           not used
	 */
	public static void main(String[] args) {
		MainFrame f = new MainFrame();
		f.launch();
	}
	
}
