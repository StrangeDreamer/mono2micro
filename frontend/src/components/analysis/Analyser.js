import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Form, DropdownButton, Dropdown, Button, Breadcrumb } from 'react-bootstrap';
import BootstrapTable from 'react-bootstrap-table-next';
import filterFactory, { numberFilter } from 'react-bootstrap-table2-filter';

var HttpStatus = require('http-status-codes');

export class Analyser extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebases: [],
            codebase: {},
            profiles: [],
            selectedProfiles: [],
            experts: [],
            expert: {},
            resultData: []
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.importResults = this.importResults.bind(this);
    }

    componentDidMount() {
        this.loadCodebases()
    }

    loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases().then(response => {
            this.setState({
                codebases: response.data
            });
        });
    }

    setCodebase(codebase) {
        this.setState({
            codebase: codebase,
            profiles: Object.keys(codebase.profiles),
            experts: codebase.dendrograms.map(dendrogram => dendrogram.graphs)
                                            .flat()
                                            .filter(graph => graph.expert === true)
        });
    }

    selectProfile(profile) {
        if (this.state.selectedProfiles.includes(profile)) {
            let filteredArray = this.state.selectedProfiles.filter(p => p !== profile);
            this.setState({
                selectedProfiles: filteredArray
            });
        } else {
            this.setState({
                selectedProfiles: [...this.state.selectedProfiles, profile]
            });
        }
    }

    setExpert(expert) {
        this.setState({
            expert: expert
        });
    }

    handleSubmit(event) {
        event.preventDefault()

        this.setState({
            isUploaded: "Uploading..."
        });

        const service = new RepositoryService();
        service.analyser(this.state.codebase.name, this.state.expert, this.state.selectedProfiles).then(response => {
            if (response.status === HttpStatus.OK) {
                this.setState({
                    isUploaded: "Upload completed successfully."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    importResults() {
        const service = new RepositoryService();
        service.importAnalyserResults(this.state.codebase.name).then(response => {
            this.setState({
                resultData: Object.values(response.data)
            });
        });
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item active>Analyser</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    render() {

        const metricRows = this.state.resultData.map((data, index) => {
            return {
                id: index,
                access: data.accessWeight,
                write: data.writeWeight,
                read: data.readWeight,
                sequence: data.sequenceWeight,
                numberClusters: data.numberClusters,
                maxClusterSize: data.maxClusterSize,
                cohesion: data.cohesion,
                coupling: data.coupling,
                complexity: data.complexity,
                fmeasure: data.fmeasure,
                accuracy: data.accuracy,
                precision: data.precision,
                recall: data.recall,
                specificity: data.specificity
            } 
        });

        const metricColumns = [{
            dataField: 'access',
            text: 'Access',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'write',
            text: 'Write',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'read',
            text: 'Read',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'sequence',
            text: 'Sequence',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'numberClusters',
            text: 'Number Clusters',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'maxClusterSize',
            text: 'Max Cluster Size',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'cohesion',
            text: 'Cohesion',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'coupling',
            text: 'Coupling',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'complexity',
            text: 'Complexity',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'fmeasure',
            text: 'F-Score',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'accuracy',
            text: 'Accuracy',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'precision',
            text: 'Precision',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'recall',
            text: 'Recall',
            sort: true,
            filter: numberFilter()
        }, {
            dataField: 'specificity',
            text: 'Specificity',
            sort: true,
            filter: numberFilter()
        }];

        return (
            <div>
                {this.renderBreadCrumbs()}
                <h4 style={{color: "#666666"}}>Analyser</h4>

                <Form onSubmit={this.handleSubmit}>
                    <Form.Group as={Row} controlId="codebase">
                        <Form.Label column sm={3}>
                            Codebase
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton title={Object.keys(this.state.codebase).length === 0 ? "Select Codebase" : this.state.codebase.name}>
                                {this.state.codebases.map(codebase => 
                                    <Dropdown.Item 
                                        key={codebase.name}
                                        onClick={() => this.setCodebase(codebase)}>{codebase.name}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="selectControllerProfiles">
                        <Form.Label column sm={3}>
                            Select Controller Profiles
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton title={'Controller Profiles'}>
                                {this.state.profiles.map(profile =>
                                    <Dropdown.Item
                                        key={profile}
                                        onSelect={() => this.selectProfile(profile)}
                                        active={this.state.selectedProfiles.includes(profile)}>{profile}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="expert">
                        <Form.Label column sm={3}>
                            Expert
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton title={Object.keys(this.state.expert).length === 0 ? "Select Expert Cut" : this.state.expert.name}>
                                {this.state.experts.map(expert => 
                                    <Dropdown.Item 
                                        key={expert.name}
                                        onClick={() => this.setExpert(expert)}>{expert.name}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 3 }}>
                            <Button type="submit"
                                    disabled={this.state.isUploaded === "Uploading..." ||
                                            Object.keys(this.state.codebase).length === 0 ||
                                            this.state.selectedProfiles.length === 0 ||
                                            Object.keys(this.state.expert).length === 0}>
                                Submit
                            </Button>
                            <Form.Text>
                                {this.state.isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                <Button className="mb-2" disabled={Object.keys(this.state.codebase).length === 0}
                        onClick={this.importResults}>
                    Import Results
                </Button>

                <BootstrapTable bootstrap4 keyField='id' data={ metricRows } columns={ metricColumns } filter={ filterFactory() }/>
            </div>
        )
    }
}