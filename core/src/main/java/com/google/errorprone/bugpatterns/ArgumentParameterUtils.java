/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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
 */

package com.google.errorprone.bugpatterns;
import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for argument and parameter name analysis.
 *
 * @author yulissa@google.com (Yulissa Arroyo-Paredes)
 */
public class ArgumentParameterUtils {
  public static final List<String> BLACK_LIST =
      ImmutableList.of("message", "counter", "index", "object", "value", "item", "key");

  /**
   * Given the name of the argument and parameter this measures the similarity of the two strings.
   * The terms within the argument and parameter names are split, so "keepPath" becomes &lt;"keep",
   * "path"&gt;.
   *
   * @return percentage of how many terms are similar between the argList and paramList. There is a
   *     false positive if given terms like "fooBar" and "barFoo", the strings are not the same but
   *     the similarity is still 1.
   */
  public static double lexicalSimilarity(String arg, String param) {
    Set<String> argSplit = splitStringTermsToSet(arg);
    Set<String> paramSplit = splitStringTermsToSet(param);

    double commonTerms = Sets.intersection(argSplit, paramSplit).size() * 2;
    double totalTerms = argSplit.size() + paramSplit.size();
    return (commonTerms / totalTerms);
  }

  /**
   * @return list of doubles that stand for similarities between the argument and the parameter at
   *     that index.
   */
  public static List<Double> similarityOfArgToParams(String arg, List<String> params) {
    List<Double> simToParams = new ArrayList<>();
    for (String param : params) {
      simToParams.add(lexicalSimilarity(arg, param));
    }
    return simToParams;
  }

  /**
   * Finds the maximum percentage and then gives the argument in that same index.
   *
   * @param tempPercentage is the array of the lexical similarity between the current parameter and
   *     the arguments, in the order that the arguments originally where
   * @param paramIndex is the parameter looking for the most similar argument
   * @param argList is just the list of arguments
   * @return once the index of which argument has the highest percentage, that index will be
   *     retrieved from the orignal argList. The argument from argList will be suggested as the new
   *     argument for the parameter.
   */
  public static String correctArgForParam(
      double[] tempPercentage, int paramIndex, List<String> argList) {
    // for now just finding the max but I am concerned for when there isn't a clear value that is
    // greater than the other. If the array is something like [.5, .5, .5].
    int maxIndex = 0;
    double max = tempPercentage[maxIndex];
    for (int i = 1; i < tempPercentage.length; i++) {
      if (max < tempPercentage[i]) {
        max = tempPercentage[i];
        maxIndex = i;
      }
    }

    return argList.get(maxIndex);
  }

  private static HashSet<String> splitStringTermsToSet(String name) {
    // TODO(yulissa): Handle constants in the form of upper underscore
    String nameSplit = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    return Sets.newHashSet(
        Splitter.on('_').trimResults().omitEmptyStrings().splitToList(nameSplit));
  }
}
