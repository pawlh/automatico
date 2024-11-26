import type {CanvasSection, Phase, Submission, User } from '@/types/types'
import type {Option} from "@/views/AutograderViews/AdminView/Analytics.vue";
import { ServerCommunicator } from '@/network/ServerCommunicator'

export const usersGet = (): Promise<User[]> => {
    return ServerCommunicator.getRequestGuaranteed<User[]>('/api/admin/users', [])
}

export const submissionsForUserGet = (netId: string): Promise<Submission[]> => {
    return ServerCommunicator.getRequestGuaranteed<Submission[]>('/api/admin/submissions/student/' + netId, [])
}

export const approveSubmissionPost = (netId: string, phase: Phase, penalize: boolean) => {
    return ServerCommunicator.postRequest('/api/admin/submissions/approve', {
        netId,
        phase,
        penalize,
    })
}

export const submissionsLatestGet = (batchSize?: number): Promise<Submission[]> => {
    batchSize = batchSize ? batchSize : -1
    return ServerCommunicator.getRequestGuaranteed<Submission[]>('/api/admin/submissions/latest/' + batchSize, [])
}

export const testStudentModeGet = (): Promise<null> => {
    return ServerCommunicator.getRequestGuaranteed<null>('/api/admin/test_mode', null)
}

type QueueStatusResponse = {
    currentlyGrading: string[],
    inQueue: string[]
}
export const getQueueStatus = (): Promise<QueueStatusResponse> => {
    return ServerCommunicator.getRequestGuaranteed<QueueStatusResponse>(
      '/api/admin/submissions/active', {
                currentlyGrading: [],
                inQueue: []
            })
}

export const commitAnalyticsGet = async (option: Option): Promise<string> => {
    try {
        return (await ServerCommunicator.doUnprocessedRequest("GET", '/api/admin/analytics/commit/' + option)).text()
    } catch (e) {
        return ''
    }
}

export const honorCheckerZipGet = async (section: number): Promise<Blob> => {
    try {
        return (await ServerCommunicator.doUnprocessedRequest("GET", '/api/admin/honorChecker/zip/' + section)).blob()
    } catch (e) {
        return new Blob()
    }
}

export const sectionsGet = (): Promise<CanvasSection[]> => {
    return ServerCommunicator.getRequestGuaranteed<CanvasSection[]>('/api/admin/sections', [])
}
