package mergeDexFiles;
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MergeDexFiles {

	public static void main(String[] args){
		
		if(args.length != 3){
			System.out.println("Please enter the paths of the two dex files to be merged, "
                + " and the path of the target dex file.");
			return;
		}
		MergeDexFiles myMerger = new MergeDexFiles();
		try {
            // myMerger.mergeDexes(args[0], args[1], args[2]);

            Dex d1 = new Dex(new File(args[0]));
            Dex d2 = new Dex(new File(args[1]));
            Dex merged = new DexMerger(d1,d2,CollisionPolicy.KEEP_FIRST).merge();
            File target = new File(args[2]);
            target.createNewFile();
            merged.writeTo(target);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
