/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.maddyhome.idea.vim.api

import com.maddyhome.idea.vim.helper.RWLockLabel
import com.maddyhome.idea.vim.state.mode.Mode
import com.maddyhome.idea.vim.state.mode.SelectionType

interface VimVisualMotionGroup {
  val exclusiveSelection: Boolean
  val selectionAdj: Int

  /**
   * Enters Visual mode, ensuring that the caret's selection start offset is correctly set
   *
   * Use this to programmatically enter Visual mode. Note that it does not modify the editor's selection.
   *
   * This overload needs to remain for compatibility with external IdeaVim extensions
   */
  fun enterVisualMode(editor: VimEditor, selectionType: SelectionType): Boolean {
    return enterVisualMode(editor, selectionType, Mode.NORMAL())
  }

  /**
   * Enters Visual mode, ensuring that the caret's selection start offset is correctly set
   *
   * Use this to programmatically enter Visual mode. Note that it does not modify the editor's selection. You can
   * specify the mode to return to when Visual command exits. Typically, this is [Mode.NORMAL], but can be [Mode.INSERT]
   * or [Mode.REPLACE] for "Insert Visual" or [Mode.SELECT] when processing a single Visual command in Select mode.
   */
  fun enterVisualMode(editor: VimEditor, selectionType: SelectionType, returnTo: Mode): Boolean

  /**
   * Enter Select mode with the given selection type
   *
   * When used from Normal, Insert or Replace modes, it will enter Select mode using the current mode as the "return to"
   * mode. I.e., if entered from Normal, will return to Normal. If entered from Insert or Replace (via shifted keys)
   * will return to Insert or Replace (aka "Insert Select" mode).
   *
   * While it will toggle between Visual and Select modes, it doesn't update the character positions correctly. IdeaVim
   * treats Select mode as exclusive and adjusts the character position when toggling modes.
   */
  fun enterSelectMode(editor: VimEditor, selectionType: SelectionType): Boolean

  /**
   * This function toggles visual mode according to the logic required for `v`, `V` and `<C-V>`
   *
   * This is the implementation for `v`, `V` and `<C-V>`. If you need to enter Visual mode, use [enterVisualMode].
   *
   * * If visual mode is disabled, enable it
   * * If visual mode is enabled, but [selectionType] differs, update visual according to new [selectionType]
   * * If visual mode is enabled with the same [selectionType], disable it
   */
  fun toggleVisual(
    editor: VimEditor,
    count: Int,
    rawCount: Int,
    selectionType: SelectionType,
    returnTo: Mode? = null,
  ): Boolean

  /**
   * Toggles between Select and Visual modes
   *
   * IdeaVim treats Select mode as always exclusive, regardless of the value in `'selection'`. As such, when toggling
   * between Visual and Select, the caret is adjusted to be more natural for exclusive selection. Specifically, when
   * toggling from Visual with inclusive selection to Select (always exclusive), the caret is adjusted one character to
   * the right, to put it as exclusive to the current selection. When toggling from Select (exclusive) to Visual with
   * inclusive selection, the caret is adjusted one character to the left from exclusive position to inclusive.
   */
  fun toggleSelectVisual(editor: VimEditor)

  /**
   * When in Select mode, enter Visual mode for a single command
   *
   * While the Vim docs state that this is for the duration of a single Visual command, it also includes motions. This
   * is different to "Insert Visual" mode (`i<C-O>v`) which allows multiple motions until an operator is invoked.
   *
   * If already in Visual, this function will return to Select.
   *
   * See `:help v_CTRL-O`.
   */
  fun processSingleVisualCommand(editor: VimEditor)

  /**
   * Detect the current selection type based on the editor's current selection state
   *
   * If the IDE changes the selection, this function can be used to understand what the current selection type is.
   */
  @RWLockLabel.Readonly
  fun detectSelectionType(editor: VimEditor): SelectionType
}
