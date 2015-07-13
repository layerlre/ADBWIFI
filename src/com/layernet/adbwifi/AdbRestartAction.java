package com.layernet.adbwifi;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * Created by layer on 9/7/2558.
 */
public class AdbRestartAction extends AdbAction {
    @Override
    public void actionPerformed(AnActionEvent e, Project project) {
        AdbUSBRestart.restart();
    }
}
